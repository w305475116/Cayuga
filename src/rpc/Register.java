package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Register
 */
@WebServlet("/register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
        // TODO Auto-generated constructor stub
    }



	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		DBConnection conn = DBConnectionFactory.getDBConnection("mysql");
//		DBConnection conn = DBConnectionFactory.getDBConnection("mongoDB");
		
		try {		
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			String firstname = input.getString("first_name");
			String lastname = input.getString("last_name");
			if(conn.registerUser(userId, password, firstname, lastname)) {
				RpcHelper.writeJsonObject(response, new JSONObject().put("status:", "OK"));
			}
			else {
				RpcHelper.writeJsonObject(response, new JSONObject().put("status:", "User Already Exist"));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			conn.close();
		}
	}
	
}


