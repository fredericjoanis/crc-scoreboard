package com.backend.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;

import com.backend.models.School;
import com.backend.models.enums.Division;
import com.backend.models.enums.TeamEnum;
import com.framework.helpers.Helpers;
import com.framework.models.Essentials;

@WebServlet("/admin/schools")
public class SchoolController extends HttpServlet
{
	private static final long serialVersionUID = 2943193194399939430L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try(Essentials essentials = Essentials.createEssentials(request,  response))
		{
			showPage(essentials);
		}
	}	
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try(Essentials essentials = Essentials.createEssentials(request,  response))
		{
			String schoolName	= Helpers.getParameter("schoolName", String.class, essentials);
			String 	action 		= Helpers.getParameter("action", String.class, essentials);
			
			ObjectId id = null;
			if(action.equals("edit") || action.equals("delete"))
			{
				id 	= Helpers.getParameter("id", ObjectId.class, essentials);
			}
			
			if(action.equals("create") || action.equals("edit"))
			{
				Division division 	= Division.valueOf(Helpers.getParameter("division", String.class, essentials));
				
				School school = new School(id, schoolName, division);
				essentials.database.save(school);
			}
			else if(action.equals("delete"))
			{
				essentials.database.remove(School.class, id);
			}
			showPage(essentials);
		}
	}
	
	private void showPage(Essentials essentials) throws ServletException, IOException
	{
		essentials.request.setAttribute("schools", School.getSchools(essentials));
		essentials.request.setAttribute("errorList", essentials.errorList);
		essentials.request.getRequestDispatcher("/WEB-INF/admin/schools.jsp").forward(essentials.request, essentials.response);
	}
}
