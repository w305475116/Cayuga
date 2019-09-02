package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		DBConnection conn = DBConnectionFactory.getDBConnection("mysql");
		try {
			//check if the session already exist
			HttpSession session = request.getSession(false);
			JSONObject obj = new JSONObject();
			if(session != null) {
				String userId = session.getAttribute("user_id").toString();
				String name = conn.getFullName(userId);
				obj.put("status", "OK").put("user_id", userId).put("name", name);
			}else {
				obj.put("status", "Invalid session");
				response.setStatus(403);
			}
			RpcHelper.writeJsonObject(response, obj);
		}
		catch(Exception e) {
			e.printStackTrace();
		}finally {
			conn.close();
		}
		
		
		
//		DBConnection connection = DBConnectionFactory.getConnection();
//		try {
//		HttpSession session = request.getSession(false);
//		JSONObject obj = new JSONObject();
//		if (session != null) {
//		String userId = session.getAttribute("user_id").toString();
//		obj.put("status", "OK").put("user_id", userId).put("name",
//		connection.getFullname(userId));
//		} else {
//		}
//		obj.put("status", "Invalid Session");
//		response.setStatus(403);
//		RpcHelper.writeJsonObject(response, obj);
//		} catch (Exception e) {
//		} finally {
//		e.printStackTrace();
//		connection.close();
//		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//because this is a post request, the session must not exist.
		//The post is sent from the form in the front end, it is a login request
		DBConnection conn = DBConnectionFactory.getDBConnection("mysql");
		JSONObject obj = new JSONObject();
		try {
			//check if the session already exist
			
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			if(conn.verifyLogin(userId, password)) {
				HttpSession session = request.getSession();//verified successfully, create a session
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(600);//maximum 10 minutes
				obj.put("status", "OK").put("user_id", userId).put("name", conn.getFullName(userId));
			}else {
				obj.put("status", "User doesn't exist or password doesn't match");
				response.setStatus(401);
			}
			RpcHelper.writeJsonObject(response, obj);
		}
		catch(Exception e) {
			e.printStackTrace();
		}finally {
			conn.close();
		}
	}

}
