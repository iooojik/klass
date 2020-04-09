package iooojik.app.klass.profile;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import iooojik.app.klass.Api;
import iooojik.app.klass.AppСonstants;
import iooojik.app.klass.Database;
import iooojik.app.klass.R;
import iooojik.app.klass.models.ServerResponse;
import iooojik.app.klass.models.profileData.Group;
import iooojik.app.klass.models.profileData.ProfileData;
import iooojik.app.klass.models.profileData.User;
import iooojik.app.klass.models.pupil.DataPupilList;
import iooojik.app.klass.models.pupil.PupilGroups;
import iooojik.app.klass.models.teacher.AddGroupResult;
import iooojik.app.klass.models.teacher.DataGroup;
import iooojik.app.klass.models.teacher.GroupInfo;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Profile extends Fragment implements View.OnClickListener {
    public Profile(){}

    private View view;
    private FloatingActionButton fab;
    private String teacherRole = "учитель", pupilRole = "ученик";
    private String userRole = "";
    private GroupsAdapter groupsAdapter;
    private Context context;
    private Fragment fragment;
    private SharedPreferences preferences;
    private String email, fullName, role, userName;
    private Api api;
    private NavController navController;
    private Database mDbHelper;
    private Cursor userCursor;
    private SQLiteDatabase mDb;
    private ImageView error;
    private View header;



    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        error = view.findViewById(R.id.errorImg);
        error.setVisibility(View.GONE);

        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        header = navigationView.getHeaderView(0);
        header.setPadding(0, 110, 0, 80);

        preferences = getActivity().getSharedPreferences(AppСonstants.APP_PREFERENCES, Context.MODE_PRIVATE);

        //получаем fab и ставим слушатель на него
        fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(this);

        //получение текущего фрагмента, чтобы использовать его в адаптере
        fragment = this;
        //контекст
        context = getContext();

        //получение пользовательской информации
        setUserInformation();

        //запрос на разрешение использования камеры
        int permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (!(permissionStatus == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 1);
        }
        //
        ImageView settings = view.findViewById(R.id.settings);
        settings.setOnClickListener(this);

        navController = NavHostFragment.findNavController(this);

        setHeaderInformation();

        return view;
    }

    private void getActiveTests() {
        //получаем к каким группам относится пользователь
        doRetrofit();
        Call<ServerResponse<DataPupilList>> responseCall = api.getPupilActiveGroups(AppСonstants.X_API_KEY,
                preferences.getString(AppСonstants.AUTH_SAVED_TOKEN, ""),
                "email", email);

        responseCall.enqueue(new Callback<ServerResponse<DataPupilList>>() {
            @Override
            public void onResponse(Call<ServerResponse<DataPupilList>> call, Response<ServerResponse<DataPupilList>> response) {
                if (response.code() == 200){
                    DataPupilList dataPupilList = response.body().getData();
                    List<PupilGroups> pupilGroups = dataPupilList.getPupilGroups();
                    PupilGroupsAdapter groupsAdapter = new PupilGroupsAdapter(pupilGroups, fragment, context);
                    if (pupilGroups.size() != 0) {
                        TextView warn = view.findViewById(R.id.nothing);
                        warn.setVisibility(View.GONE);
                    }

                    RecyclerView recyclerView = view.findViewById(R.id.classes);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    recyclerView.setAdapter(groupsAdapter);
                }else {
                    Log.e("GET PUPIL GROUPS", String.valueOf(response.raw()));
                }
            }

            @Override
            public void onFailure(Call<ServerResponse<DataPupilList>> call, Throwable t) {
                Log.e("GET PUPIL GROUPS", String.valueOf(t));
            }
        });
    }

    private void setHeaderInformation(){

        TextView name = header.findViewById(R.id.textView);
        TextView emailText = header.findViewById(R.id.textView2);

        name.setText(fullName);
        emailText.setText(email);
    }

    private void setProfileBackground(){
        FrameLayout frameLayout = view.findViewById(R.id.backroundProfile);
        if (preferences.getInt(AppСonstants.BACKGROUND_PROFILE, -1) != -1)
        frameLayout.setBackgroundResource(preferences.getInt(AppСonstants.BACKGROUND_PROFILE, -1));

    }

    private void setUserInformation() {
        //получаем и устанавливаем пользовательскую информацию
        setProfileBackground();
        TextView Email = view.findViewById(R.id.email);
        TextView name = view.findViewById(R.id.name);

        mDbHelper = new Database(getContext());
        mDbHelper.openDataBase();
        mDbHelper.updateDataBase();
        mDb = mDbHelper.getWritableDatabase();
        userCursor =  mDb.rawQuery("Select * from Profile WHERE _id=?",
                new String[]{String.valueOf(0)});

        mDb = mDbHelper.getReadableDatabase();
        userCursor.moveToFirst();

        fullName = userCursor.getString(userCursor.getColumnIndex("full_name"));
        email = userCursor.getString(userCursor.getColumnIndex("email"));
        userName = userCursor.getString(userCursor.getColumnIndex("username"));
        Email.setText(email);
        name.setText(fullName);
        getUserProfile();

        /**
         * 1. если стоит учительский профиль, то убираем ученический профиль, изменяем поле "роль",
         * показываем fab и ставим адаптер для RecyclerView(id = classes). GroupsAdapter(контекст, список с группами, текущий фрагмент)
         * 2. если ученический профиль, то убираем учительский, ставим соответсвующую роль, убираем fab
         * и ставим адаптер на RecyclerView(id = teachers) и получаем список активных тестов
         */

    }

    private void getUserProfile(){
        doRetrofit();

        Call<ServerResponse<ProfileData>> call = api.getUserDetail(AppСonstants.X_API_KEY,
                preferences.getString(AppСonstants.AUTH_SAVED_TOKEN, ""), getUserID());

        call.enqueue(new Callback<ServerResponse<ProfileData>>() {
            @Override
            public void onResponse(Call<ServerResponse<ProfileData>> call, Response<ServerResponse<ProfileData>> response) {
                if (response.code() == 200){
                    ProfileData profileData = response.body().getData();
                    User user = profileData.getUser();
                    Group group = user.getGroup().get(user.getGroup().size() - 1);
                    switch (group.getName().toLowerCase()){
                        case "teacher":
                            userRole = teacherRole;
                            getGroupsFromDatabase();
                            break;
                        case "pupil":
                            userRole = pupilRole;
                            getActiveTests();
                            break;
                    }
                    TextView role = view.findViewById(R.id.role);
                    role.setText(userRole);
                    if (!user.getAvatar().isEmpty()) {
                        ImageView avatar = view.findViewById(R.id.avatar);
                        Picasso.get().load(AppСonstants.IMAGE_URL + user.getAvatar())
                                .resize(100, 100)
                                .transform(new RoundedCornersTransformation(30, 5)).into(avatar);

                        ImageView profileImg = header.findViewById(R.id.imageView2);

                        Picasso.get().load(AppСonstants.IMAGE_URL + user.getAvatar())
                                .resize(100, 100)
                                .transform(new RoundedCornersTransformation(30, 5)).into(profileImg);


                    }
                }
            }

            @Override
            public void onFailure(Call<ServerResponse<ProfileData>> call, Throwable t) {

            }
        });
    }

    private int getUserID(){
        int userId = -1;
        mDbHelper = new Database(context);
        mDbHelper = new Database(context);
        mDbHelper.openDataBase();
        mDbHelper.updateDataBase();
        mDb = mDbHelper.getReadableDatabase();
        userCursor = mDb.rawQuery("Select * from Profile WHERE _id=?", new String[]{String.valueOf(0)});
        userCursor.moveToFirst();

        userId = Integer.parseInt(userCursor.getString(userCursor.getColumnIndex("id")));
        return userId;
    }

    private void doRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppСonstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(Api.class);
    }

    private void getGroupsFromDatabase(){
        fab.show();
        fab.setImageResource(R.drawable.round_add_24);
        doRetrofit();
        Call<ServerResponse<DataGroup>> response = api.getGroups(AppСonstants.X_API_KEY, "author_email", email);

        response.enqueue(new Callback<ServerResponse<DataGroup>>() {
            @Override
            public void onResponse(Call<ServerResponse<DataGroup>> call, Response<ServerResponse<DataGroup>> response) {
                if(response.code() == 200) {
                    ServerResponse<DataGroup> result = response.body();
                    List<GroupInfo> groupInforms = result.getData().getGroupInfos();
                    if (groupInforms.size() != 0) {
                        TextView warn = view.findViewById(R.id.nothing);
                        warn.setVisibility(View.GONE);
                    }
                    groupsAdapter = new GroupsAdapter(context, groupInforms, fragment);
                    RecyclerView recyclerView = view.findViewById(R.id.classes);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    recyclerView.setAdapter(groupsAdapter);

                } else {
                    Log.e("GETTING GROUPS", response.raw() + "");
                }
            }
            @Override
            public void onFailure(Call<ServerResponse<DataGroup>> call, Throwable t) {
                Log.e("GETTING GROUPS",t.toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                //добавление класса в учительский профиль
                if(userRole.equals(teacherRole)){
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                    View view1 = getLayoutInflater().inflate(R.layout.edit_text, null);
                    TextInputLayout textInputLayout = view1.findViewById(R.id.text_input_layout);
                    textInputLayout.setHint("Введите класс");
                    textInputLayout.setCounterMaxLength(3);
                    EditText name = view1.findViewById(R.id.edit_text);

                    builder.setView(view1);

                    builder.setPositiveButton("Добавить", (dialog, which) -> {
                        //заносим в базу данных
                        doRetrofit();

                        error.setVisibility(View.GONE);
                        TextView warn = view.findViewById(R.id.nothing);
                        warn.setVisibility(View.GONE);

                        String nameGroup = name.getText().toString();

                        HashMap<String, String> post = new HashMap<>();
                        post.put("author_email", email);
                        post.put("name", nameGroup);
                        post.put("test", "q");
                        post.put("author_name", fullName);

                        Log.e("creds", email + " " + nameGroup + " ");

                        Call<ServerResponse<AddGroupResult>> responseCall = api.addGroup(
                                AppСonstants.X_API_KEY, preferences.getString(AppСonstants.AUTH_SAVED_TOKEN, ""),
                                post);

                        responseCall.enqueue(new Callback<ServerResponse<AddGroupResult>>() {
                            @Override
                            public void onResponse(Call<ServerResponse<AddGroupResult>> call, Response<ServerResponse<AddGroupResult>> response) {
                                if (response.code() != 200){
                                    Log.e("Add Group", String.valueOf(response.raw()));
                                }else {
                                    getGroupsFromDatabase();
                                }
                            }

                            @Override
                            public void onFailure(Call<ServerResponse<AddGroupResult>> call, Throwable t) {
                                Log.e("Add Group", String.valueOf(t));
                            }
                        });

                    });
                    builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
                    builder.create().show();
                }
                break;
            case R.id.settings:
                navController.navigate(R.id.nav_settings);
                break;
        }
    }

}
