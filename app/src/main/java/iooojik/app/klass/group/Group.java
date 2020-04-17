package iooojik.app.klass.group;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;

import iooojik.app.klass.Api;
import iooojik.app.klass.AppСonstants;
import iooojik.app.klass.R;
import iooojik.app.klass.models.PostResult;
import iooojik.app.klass.models.ServerResponse;
import iooojik.app.klass.models.TestResults.DataTestResult;
import iooojik.app.klass.models.TestResults.TestsResult;
import iooojik.app.klass.models.matesList.DataUsersToGroup;
import iooojik.app.klass.models.matesList.Mates;
import iooojik.app.klass.models.paramUsers.Data;
import iooojik.app.klass.models.paramUsers.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Group extends Fragment implements View.OnClickListener{

    public Group() {}

    private View view;
    private int groupID = -1;
    private String groupName;
    private String groupAuthor;
    private String groupAuthorName;
    private int id = -1;
    private Context context;
    private GroupMatesAdapter groupmatesAdapter;
    private FloatingActionButton fab;
    private Api api;
    private Fragment fragment;
    private List<Mates> mates;
    private SharedPreferences preferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group, container, false);
        preferences = getActivity().getSharedPreferences(AppСonstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        //получение названия нажатого класса
        getGroupInfo();
        //контекст
        context = getContext();
        //получение списка одноклассников
        getGroupMates();

        RecyclerView groupmates = view.findViewById(R.id.groupmates);
        groupmates.setLayoutManager(new LinearLayoutManager(context));
        groupmates.setAdapter(groupmatesAdapter);
        //конпка с открытием редактора тестов
        Button testEditor = view.findViewById(R.id.testEditor);
        testEditor.setOnClickListener(this);
        fragment = this;

        fab = getActivity().findViewById(R.id.fab);
        fab.show();
        fab.setOnClickListener(this);

        return view;
    }

    private void doRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppСonstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(Api.class);
    }

    private void getGroupMates(){

        //получаем список учеников(их полное имя и email) из бд
        doRetrofit();
        Call<ServerResponse<DataUsersToGroup>> response = api.getMatesList(AppСonstants.X_API_KEY, "group_id", String.valueOf(id));

        response.enqueue(new Callback<ServerResponse<DataUsersToGroup>>() {
            @Override
            public void onResponse(Call<ServerResponse<DataUsersToGroup>> call, Response<ServerResponse<DataUsersToGroup>> response) {
                if(response.code() == 200) {
                    ServerResponse<DataUsersToGroup> result = response.body();
                    mates = result.getData().getMates();

                    Call<ServerResponse<DataTestResult>> call2 = api.getTestResults(AppСonstants.X_API_KEY,
                            preferences.getString(AppСonstants.AUTH_SAVED_TOKEN, ""), "group_id", String.valueOf(id));
                    call2.enqueue(new Callback<ServerResponse<DataTestResult>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<DataTestResult>> call, Response<ServerResponse<DataTestResult>> response) {
                            if (response.code() == 200){
                                DataTestResult result = response.body().getData();
                                List<TestsResult> testsResults = result.getTestsResult();
                                groupmatesAdapter = new GroupMatesAdapter(context, fragment, mates, testsResults);
                                RecyclerView recyclerView = view.findViewById(R.id.groupmates);
                                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                recyclerView.setAdapter(groupmatesAdapter);
                            }
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<DataTestResult>> call, Throwable t) {

                        }
                    });


                } else {
                    Log.e("GETTING MATES", response.raw() + "");
                }
            }

            @Override
            public void onFailure(Call<ServerResponse<DataUsersToGroup>> call, Throwable t) {
                Log.e("GETTING MATES",t.toString());
                fab.hide();
                ImageView error = view.findViewById(R.id.errorImg);
                error.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getGroupInfo(){
        Bundle args = this.getArguments();
        groupID = args.getInt("groupID");
        groupAuthor = args.getString("groupAuthor");
        groupName = args.getString("groupName");
        groupAuthorName = args.getString("groupAuthorName");
        id = args.getInt("id");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                /**MaterialAlertDialogBuilder для добавления нового ученика в группу
                 * 1. пользователь вводит email и полное имя ученика, если он есть в базе, то
                 * он добавляется в список и, соответсвенно, в бд
                 */

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                View view1 = getLayoutInflater().inflate(R.layout.edit_text, null);
                TextInputEditText emailText = view1.findViewById(R.id.edit_text);
                emailText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                TextInputLayout textInputLayout = view1.findViewById(R.id.text_input_layout);
                textInputLayout.setHint("Введите e-mail адрес");
                textInputLayout.setHelperTextEnabled(false);
                textInputLayout.setCounterEnabled(false);

                layout.addView(view1);

                builder.setPositiveButton("Добавить", (dialog, which) -> {
                    String email = emailText.getText().toString().trim();
                    boolean result = false;

                    //проверяем, есть ли пользовтель в группе
                    if (mates.size() == 0 && !email.equals(groupAuthor)) {
                        result = true;
                    }else {

                        for (Mates mate : mates) {
                            if (email.equals(mate.getEmail())) {
                                result = false;
                                break;
                            } else result = true;
                        }

                    }

                    //получаем пользовательскую информацию по email
                    //если код == 200, то заносим пользователя в группу
                    //иначе выдаём сообщение об ошибке

                    if (result){
                        addNewUser(email);
                    }
                    else Snackbar.make(view, "Пользователь с указанным email-адресом уже есть в группе", Snackbar.LENGTH_LONG).show();

                });

                builder.setView(layout);
                builder.create().show();
                break;
            case R.id.testEditor:
                //редактор тестов
                Bundle bundle = new Bundle();
                bundle.putInt("id", id);
                bundle.putInt("groupID", groupID);
                bundle.putString("groupAuthor", groupAuthor);
                bundle.putString("groupAuthorName", groupAuthorName);
                bundle.putString("groupName", groupName);
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.nav_testEditor, bundle);
        }
    }

    private void addNewUser(String email) {
        //получение информации о добавляемом пользователе

        doRetrofit();

        Call<ServerResponse<Data>> call = api.getParamUser(AppСonstants.X_API_KEY,
                preferences.getString(AppСonstants.AUTH_SAVED_TOKEN, ""),"email", email);

        call.enqueue(new Callback<ServerResponse<Data>>() {
            @Override
            public void onResponse(Call<ServerResponse<Data>> call, Response<ServerResponse<Data>> response) {
                if (response.code() == 200){

                    Data data = response.body().getData();
                    User user = data.getUser().get(0);

                    String avatar = user.getAvatar();
                    if (avatar == null || avatar.isEmpty()){
                        avatar = "null";
                    }

                    HashMap<String, String> map = new HashMap<>();
                    map.put("full_name", user.getFullName());
                    map.put("email", user.getEmail());
                    map.put("group_id", String.valueOf(id));
                    map.put("group_name", groupName);
                    map.put("avatar", avatar);

                    Call<ServerResponse<PostResult>> response2 = api.addUserToGroup(AppСonstants.X_API_KEY,
                            preferences.getString(AppСonstants.AUTH_SAVED_TOKEN, ""), map);

                    response2.enqueue(new Callback<ServerResponse<PostResult>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<PostResult>> call, Response<ServerResponse<PostResult>> response) {
                            if (response.code() == 200) {
                                Snackbar.make(view, "Пользователь был успешно добавлен", Snackbar.LENGTH_LONG).show();
                                getGroupMates();
                            }
                            else Log.e("ADD MATE", String.valueOf(response.raw()) + map);
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<PostResult>> call, Throwable t) {
                            Log.e("ADD MATE", String.valueOf(t));
                        }
                    });



                    } else {
                        Log.e("ttt", String.valueOf(response.raw()));
                        Snackbar.make(view, "Пользователь с указанным e-mail адресом не был найден.",
                                Snackbar.LENGTH_LONG).show();
                    }
            }
            @Override
            public void onFailure(Call<ServerResponse<Data>> call, Throwable t) {

            }
        });
    }
}
