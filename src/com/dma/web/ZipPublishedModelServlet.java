package com.dma.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

/**
 * Servlet implementation class AppendSelectionsServlet
 */
@WebServlet(name = "ZipPublishedModel", urlPatterns = { "/ZipPublishedModel" })
public class ZipPublishedModelServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ZipPublishedModelServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		Map<String, Object> parms = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();

		try {
			
			result.put("CLIENT", request.getRemoteAddr() + ":" + request.getRemotePort());
			result.put("SERVER", request.getLocalAddr() + ":" + request.getLocalPort());
			
			result.put("FROM", this.getServletName());
			
			String user = request.getUserPrincipal().getName();
			result.put("USER", user);

			result.put("JSESSIONID", request.getSession().getId());
			
			Path wks = Paths.get(getServletContext().getRealPath("/datas") + "/" + user);			
			result.put("WKS", wks.toString());
			
			Path prj = Paths.get((String) request.getSession().getAttribute("projectPath"));
			result.put("PRJ", prj.toString());

			
			parms = Tools.fromJSON(request.getInputStream());
			result.put("PARMS", parms);

			if(parms != null && parms.get("publishedModelName") != null) {
				
				String publishedModelName = (String) parms.get("publishedModelName");
				
				Path publishedModelPath = Paths.get((String) request.getServletContext().getAttribute("cognosModelsPath") + "/" +
						user + "/" + publishedModelName);
				
				File dir = publishedModelPath.toFile();

				Path dlDir = Paths.get("/tmp");
				
				Path zip = Paths.get(dlDir + "/" + user + "-" + publishedModelName + ".zip");
				
				if(dir.exists()){
					ZipUtil.pack(dir, zip.toFile(), new NameMapper() {
						public String map(String name) {
							return publishedModelName + "/" + name;
						}
					});		
				}

				if(Files.exists(zip)) {
					zip.toFile().setReadable(true, false);
					zip.toFile().setWritable(true, false);
					zip.toFile().setExecutable(true, false);
					result.put("MESSAGE", zip.toString() + " will be downloaded.");
					result.put("FILENAME", zip.toString());
					request.getServletContext().setAttribute("publishedModelPath", zip);
				}
				else {
					result.put("STATUS", "KO");
					throw new Exception(zip.toString() + " not found.");
				}
				
			}
			else {
				result.put("STATUS", "KO");
				throw new Exception("Input parameters are not valid.");
			}			
			result.put("STATUS", "OK");
			
		}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			result.put("STATUS", "KO");
			result.put("EXCEPTION", e.getClass().getName());
			result.put("MESSAGE", e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			result.put("STACKTRACE", sw.toString());
			e.printStackTrace(System.err);
		}

		finally{
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(Tools.toJSON(result));
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