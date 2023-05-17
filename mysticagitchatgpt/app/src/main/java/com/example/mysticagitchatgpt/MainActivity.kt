package com.example.mysticagitchatgpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    val HttpContentType_JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    var okHttpClient = OkHttpClient()

    val responseHandler: Handler = Handler {
        var responseText: String? = it.obj.toString()

        responseText?.let {
            var tvResponseText = findViewById<TextView>(R.id.tv_response_chatgpt)
            tvResponseText.text = it
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI 구성
        initUI()
    }

    private fun initUI() {
        var etRequestText = findViewById<EditText>(R.id.et_input_text)
        var btRequest = findViewById<Button>(R.id.bt_request_chatgpt)


        btRequest.setOnClickListener {
            var requestText: String? = etRequestText.text.toString()

            requestText?.let {
                requestChatGPT(it)
            }
        }
    }

    private fun requestChatGPT(question: String) {

        Log.d("mysticagit", "question : $question")

        // request body에 들어갈 Json
        var jsonBody: JSONObject = JSONObject()

        // API 레퍼런스 참고 ( https://platform.openai.com/docs/api-reference/completions/create )
        jsonBody.put("model", "text-davinci-003")
        jsonBody.put("prompt", question)
        jsonBody.put("max_tokens", 30)      // 응답 문자열 token 수
        jsonBody.put("temperature", 0)

        // request에 필요한 요청 url(url), API Key(header) 설정
        // (API Key 확인하기 : https://platform.openai.com/account/api-keys )
        var chatGPTApiKey = resources.getString(R.string.chatgpt_api_key)
        var requestBody: RequestBody = (jsonBody.toString()).toRequestBody(HttpContentType_JSON)
        var request: Request = Request.Builder()
            .url("https://api.openai.com/v1/completions")
            .header("Authorization", chatGPTApiKey)  // API Key 발급받은 것을 사용하세요!
            .post(requestBody)
            .build()
        // example Authorization : Bearer sk-asdf1234qwer1234zxcv9876...

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
                        responseHandler.obtainMessage(1, -1, -1, it).sendToTarget()
                    } ?: {
                        Log.d("mysticagit", "answer is empty")
                        responseHandler.obtainMessage(1, -1, -1, "(answer is empty)").sendToTarget()
                    }
                }
            }

            // 응답 실패 시
            override fun onFailure(call: Call, e: IOException) {
                Log.d("mysticagit", "onFailure")
                responseHandler.obtainMessage(1, -1, -1, "(onFailure)").sendToTarget()
            }
        })
    }
}

