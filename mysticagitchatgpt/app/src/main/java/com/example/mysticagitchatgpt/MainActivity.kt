package com.example.mysticagitchatgpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    val HttpContentType_JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    var okHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 메서드 호출
        requestChatGPT("what is apple?")
    }

    fun requestChatGPT(question: String) {

        Log.d("mysticagit", "question : $question")

        // request body에 들어갈 Json
        var jsonBody: JSONObject = JSONObject()

        // API 레퍼런스 참고 ( https://platform.openai.com/docs/api-reference/completions/create )
        jsonBody.put("model", "text-davinci-003")
        jsonBody.put("prompt", question)
        jsonBody.put("max_tokens", 30)
        jsonBody.put("temperature", 0)

        // request에 필요한 요청 url(url), API Key(header) 설정
        // (API Key 확인하기 : https://platform.openai.com/account/api-keys )
        var requestBody: RequestBody = (jsonBody.toString()).toRequestBody(HttpContentType_JSON)
        var request: Request = Request.Builder()
            .url("https://api.openai.com/v1/completions")
            .header("Authorization", "Bearer sk-AbcDeAbcdeABCDE1234567890J1234567890")  // API Key 발급받은 것을 사용하세요!
            .post(requestBody)
            .build()

        // 요청
        okHttpClient.newCall(request).enqueue( object : Callback {
            // 응답 성공 시
            override fun onResponse(call: Call, response: Response) {
                Log.d("mysticagit", "onResponse")
                if(response.isSuccessful) {
                    var jsonResponse = JSONObject(response.body?.string())

                    // response body에서 choices 내 답변된 text 데이터 확인
                    var jsonResponseArray = jsonResponse.getJSONArray("choices")
                    var result: String? = jsonResponseArray.getJSONObject(0).getString("text")

                    // 결과 출력
                    result?.let {
                        Log.d("mysticagit", "answer : $it")
                    } ?: {
                        Log.d("mysticagit", "answer is empty")
                    }
                }
            }

            // 응답 실패 시
            override fun onFailure(call: Call, e: IOException) {
                Log.d("mysticagit", "onFailure")
            }
        })
    }
}

