package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.scenario.effect.impl.state.LinearConvolveRenderState.PassType;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import rpc.RpcHelper;
import external.TicketMasterAPI;


/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);//should be false
		if(session==null) {
			response.setStatus(403);
			return;
		}
		// apikey=%s&geoPoint=%s&keyword=%s&radius=%s
		String userId = session.getAttribute("user_id").toString();//must login first to use this method
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		DBConnection conn = DBConnectionFactory.getDBConnection("mysql");
//		DBConnection conn = DBConnectionFactory.getDBConnection("mongoDB");
		try {		
			List <Item>items = conn.searchItems(lat, lon, null);
			Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
			JSONArray array = new JSONArray();
			for(Item item:items ) {
			JSONObject obj = item.toJSONObject();
			obj.put("favorite", favoriteItemIds.contains(item.getItemId()));//this field is used to display a heart shape in the front end
			array.put(obj);
			}
			RpcHelper.writeJsonArray(response, array);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			conn.close();
		}

		}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
