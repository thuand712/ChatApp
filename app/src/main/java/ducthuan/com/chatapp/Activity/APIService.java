package ducthuan.com.chatapp.Activity;

import ducthuan.com.chatapp.Notifications.MyResponse;
import ducthuan.com.chatapp.Notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAx1ZkS5w:APA91bHvWOHa460ecc397WuggdPXDqvkIdQrWWFV9YClI0e3yhlIik4RI-Pynh0uCx6rshAkorn49-gakC7TpaY6vlUOIH6NQkmy2ydQ5Mv5At6ocdk0HYfS9pZvYWP9l_jFg_uJXLNW"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
