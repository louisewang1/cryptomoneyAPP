package com.example.cryptomoney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;

import javax.xml.transform.Result;


public class LoginActivity extends AppCompatActivity {

    private EditText accountEdit;
    private EditText passwordEdit;
    private Button login;
    private Connection conn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String username = accountEdit.getText().toString();
                final String pwd = passwordEdit.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        conn = (Connection) DBOpenHelper.getConn();
                        // check username and pwd
                        Object[] result = checkuser(conn,username,pwd);
                        Log.d("LoginActivity","username: " +result[0] +" balance: " + result[1]);
                        if (result[0] != null && result[1] != null) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("username",(String) result[0]);
                            intent.putExtra("balance",(Double) result[1]);
                            intent.putExtra("email",(String) result[2]);
                            intent.putExtra("cellphone",(String) result[3]);
                            startActivity(intent);
                            finish();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "account or password is invalid", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    public static Object[] checkuser(Connection conn, String username, String pwd) {
        String sql = "select * from accountdb where username = ? and pwd = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
//        StringBuilder builder = new StringBuilder();
//        builder = null;
        Object[] result = new Object[4];
        try {
            ps = (PreparedStatement) conn.prepareStatement(sql);
            ps.setString(1,username);
            ps.setString(2,pwd);
            rs = ps.executeQuery();
            if (rs != null) {
                while(rs.next()){
                    result[0] = rs.getString("username");
                    result[1] = rs.getDouble("balance");
                    result[2] = rs.getString("email");
                    result[3] = rs.getString("cellphone");
                    return result;
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DBOpenHelper.closeConnection(conn);
    }
}