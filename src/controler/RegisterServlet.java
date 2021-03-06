package controler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;



import model.User;
import model.db.DBManager;
import model.db.UserDao;

@WebServlet("/register")
@MultipartConfig 
public class RegisterServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String AVATAR_URL = "/D:/SofyaBread/uploads/users/";

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// check for register credentials
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String password2 = request.getParameter("password2");
		String email = request.getParameter("email");
		String company = request.getParameter("company");
		Part avatarPart = request.getPart("avatar");
		
		// InputStream reads the contents of the file, save to a local file, save URL in database
		InputStream fis = avatarPart.getInputStream(); 
		File myFile = new File(AVATAR_URL + username +".jpg");
		if (!myFile.exists()){
			myFile.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(myFile);
		int b = fis.read();
		while (b != -1){
			fos.write(b);
			b = fis.read();
		}
		fis.close();
		fos.close();
		
		
		String avatarUrl = username + ".jpg"; // or = myFile.getAbsolutePath();

		if (!password.equals(password2)) {
			request.setAttribute("error", "passwords missmatch");
			request.getRequestDispatcher("register.jsp").forward(request, response);
			return;
		}
		try {
			if (!UserDao.getInstanse().existsUser(username, password)) {
				// insert user in db
				User u = new User(username, company, password, email, avatarUrl);
				UserDao.getInstanse().insertUser(u);
				// update session
				request.getSession().setAttribute("user", u);
				// redirect to welcome.jsp
				request.getRequestDispatcher("welcome.jsp").forward(request, response);
			} else {
				request.setAttribute("error", "User already registerred");
				request.getRequestDispatcher("login.jsp").forward(request, response);
				return;
			}
		} catch (SQLException e1) {
			request.setAttribute("error", "DB problem:" + e1.getMessage());
			request.getRequestDispatcher("login.jsp").forward(request, response);
		}
	}

	@Override
	public void destroy() {
		try {
			DBManager.getInstanse().closeConnection();
		} catch (SQLException e) {
			System.out.println("ops close con");
		}
		super.destroy();
	}
}
