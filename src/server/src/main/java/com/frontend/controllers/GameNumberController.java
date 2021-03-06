package com.frontend.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.backend.models.Game;
import com.framework.models.Essentials;

@WebServlet("/gameNumber")
public class GameNumberController extends HttpServlet
{
	private static final long serialVersionUID = -8713025109133551951L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try (Essentials essentials = Essentials.createEssentials(request, response)) 
		{
			Game liveGame = GameController.getLiveOrNextGame(essentials);
			essentials.request.setAttribute("game", liveGame);
			
			request.getRequestDispatcher("/WEB-INF/frontend/gameNumber.jsp").forward(request, response);
		}
	}
}
