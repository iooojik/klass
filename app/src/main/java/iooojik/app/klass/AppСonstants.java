package iooojik.app.klass;

import android.annotation.SuppressLint;
import android.hardware.Camera;

public class AppСonstants {

    // идентификатор уведомления
    public static int NOTIFY_ID = 101;
    // название настроек
    public static final String APP_PREFERENCES = "Settings";
    // тема приложения
    public static final String APP_PREFERENCES_THEME = "Theme";
    // показывать ли доп. материалы в заметках
    public static final String APP_PREFERENCES_SHOW_BOOK_MATERIALS = "Show Book Materials";
    //зарегистрирован ли пользователь
    public static final String APP_PREFERENCES_IS_AUTH = "is User Passed Auth";
    // имя файла с бд
    public static String DB_NAME = "database.db";
    // путь к бд
    public static String DB_PATH;
    // версия бд
    public static final int DB_VERSION = 50;
    // код запроса для намерения для обработки обновления play services, если это необходимо
    public static final int RC_HANDLE_GMS = 9001;
    // код разрешения
    public static final int RC_HANDLE_CAMERA_PERM = 2;
    //автофокус
    public static final String AutoFocus = "AutoFocus";
    //фонарик
    public static final String UseFlash = "UseFlash";
    //сам QR-объект
    public static final String BarcodeObject = "Barcode";
    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
    public static final String TAG = "OpenCameraSource";
    public static final int DUMMY_TEXTURE_NAME = 100;
    public static final float ASPECT_RATIO_TOLERANCE = 0.01f;
    //страница в google play market
    public static String url = "https://play.google.com/store/apps/details?id=iooogik.app.modelling";
    //
    public static final String BASE_URL = "http://195.19.44.146/service/";
    public static final String IMAGE_URL = "http://195.19.44.146/service/uploads/user/";
    public static final String X_API_KEY = "33CD9CAE7D5D15F7E435A26BBFF81A4E";
    public static String AUTH_SAVED_TOKEN = "AUTH_TOKEN";
    public static String STANDART_TOKEN = "STANDART_TOKEN";

    public static String BACKGROUND_PROFILE = "BACKGROUND_PROFILE";
    public static final String adminEmail = "test@test.com";
    public static final String adminPassword = "123456";

}
