package com.backend.views.yearly;

import java.util.Locale;

import com.backend.controllers.GameController;
import com.backend.models.Game;
import com.backend.models.enums.GameEventEnum;
import com.backend.models.enums.SideEnum;
import com.backend.models.enums.yearly.TriangleStateEnum;
import com.backend.models.yearly.GameStateYearly;
import com.backend.models.yearly.Hole;
import com.framework.helpers.LocalizedString;
import com.google.common.collect.ImmutableMap;

public class GameYearlyView 
{
	// Called by the view
	public static String getScripts()
	{
		StringBuilder strBuilder = new StringBuilder();		
		
		strBuilder.append("$('.selectColor').change(function()");
		strBuilder.append("		{");
		strBuilder.append("			if(this.value == \"EMPTY\")");
		strBuilder.append("			{");
		strBuilder.append("				$(this).css(\"background-color\", \"white\");	");
		strBuilder.append("			}");
		strBuilder.append("			else if(this.value == \"BLUE\")");
		strBuilder.append("			{");
		strBuilder.append("				$(this).css(\"background-color\", \"lightblue\");");
		strBuilder.append("			}");
		strBuilder.append("			else if(this.value == \"YELLOW\")");
		strBuilder.append("			{");
		strBuilder.append("				$(this).css(\"background-color\", \"yellow\");");
		strBuilder.append("			}");
		strBuilder.append("		})");
		
		return strBuilder.toString();
	}
	
	// Called by the view
	public static String getHtmlForGame(Game game, Locale currentLocale)
	{
		LocalizedString strScoreboardUpdate = new LocalizedString(ImmutableMap.of( 	
				Locale.ENGLISH, "Update score board", 
				Locale.FRENCH, 	"Mettre à jour le tableau de pointage"
				), currentLocale);

		LocalizedString strAdd = new LocalizedString(ImmutableMap.of( 	
				Locale.ENGLISH, "Add", 
				Locale.FRENCH, 	"Ajouter"
				), currentLocale);

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<form method=\"post\">");
		strBuilder.append("	<input type=\"hidden\" name=\"gameEvent\" value=\"" + GameEventEnum.SCOREBOARD_UPDATED.toString() + "\" />");
		strBuilder.append("	<input type=\"hidden\" name=\"id\" value=\"" + game._id + "\" />");
		strBuilder.append("	<h2>" + strScoreboardUpdate + "</h2>");

		Hole[] triangleLeft = ((GameStateYearly)(game.getGameStates().get(game.getGameStates().size() - 1))).triangleLeft;
		Hole[] triangleRight = ((GameStateYearly)(game.getGameStates().get(game.getGameStates().size() - 1))).triangleRight;

		outputTriangle(triangleLeft, SideEnum.SIDE1, strBuilder);
		outputTriangle(triangleRight, SideEnum.SIDE2, strBuilder);

		strBuilder.append("	<div class=\"clear\"></div>");
		strBuilder.append("	<br/>");
		strBuilder.append(GameController.outputAddAfterForView(game, currentLocale));
		strBuilder.append("	<br/>");
		strBuilder.append("	<input type=\"submit\" value=\"" + strAdd + "\" />");
		strBuilder.append("</form>");
		
		return strBuilder.toString();
	}

	private static void outputTriangle(Hole[] triangle, SideEnum side, StringBuilder strBuilder)
	{
		strBuilder.append("<table class=\"triangle\"><tr>");
		for(int holeNb = 0; holeNb < triangle.length; holeNb++)
		{
			/*
		       0
			  1 2
			 3 4 5
			6 7 8 9
		 */
			if(holeNb == 1 || holeNb == 3 || holeNb == 6)
			{
				strBuilder.append("</tr><tr>");
			}
			
			String firstTD = "<td>";
			if(holeNb == 0 || holeNb == 1 || holeNb == 3)
			{
				firstTD = "<td colspan=\"8\" align=\"center\"><table><tr><td>";
			}
			strBuilder.append(firstTD + holeNb + "</td>");
			
			strBuilder.append("<td width=\"90px\">");
			
			Hole hole = triangle[holeNb];
			for(int triangleStateNb = 0; triangleStateNb < hole.triangleStates.length; triangleStateNb++)
			{
				 TriangleStateEnum triangleState = hole.triangleStates[triangleStateNb];
				
				strBuilder.append("<select class=\"selectColor triangleStateEnum_" + triangleState.name() + "\" name=\"hole_" + side.toString() + "_" + holeNb + "_" + triangleStateNb + "\">");

				for(TriangleStateEnum triangleStateEnum : TriangleStateEnum.values())
				{
					String isSelected = "";
					if(triangleStateEnum == triangleState)
					{
						isSelected = " selected";
					}
					
					strBuilder.append("<option value=\"" + triangleStateEnum.name() + "\" class=\"triangleStateEnum_" + triangleStateEnum.name() + "\"" + isSelected + ">" + triangleStateEnum.name() + "</option>");
				}
				strBuilder.append("</select>");
			}
			String lastTd = "</td>";
			if(holeNb == 0 || holeNb == 2 || holeNb == 5)
			{
				lastTd = "</td></tr></table></td>";
			}
			strBuilder.append(lastTd);
	 	} 
		strBuilder.append("</tr></table>");
	}
}