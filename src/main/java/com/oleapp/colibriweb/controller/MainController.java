package com.oleapp.colibriweb.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.oleapp.colibriweb.dao.impl.PostgresAppStatisticDAO;

import lombok.Data;

@Controller
public class MainController {

	@Data
	public static class Downloads {
		private int count;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String indexPage(@ModelAttribute Downloads downloads) {
		downloads.setCount(PostgresAppStatisticDAO.getInstance().getDownloadCount());
		return "index";
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public void downloadColibriRar(HttpServletRequest request, HttpServletResponse response) {
		downloadColibri(request, response);
	}

	public void downloadColibri(HttpServletRequest request, HttpServletResponse response) {
		PostgresAppStatisticDAO.getInstance().incrementDownloadCount();
		String dataDirectory = request.getServletContext().getRealPath("/WEB-INF/downloads/");
		Path file = Paths.get(dataDirectory, "ColibriApp.rar");
		if (Files.exists(file)) {
			response.setContentType("application/vnd.rar");
			response.addHeader("Content-Disposition", "attachment; filename=ColibriApp.rar");
			try {
				Files.copy(file, response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
