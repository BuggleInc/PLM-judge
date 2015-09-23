package server.parser;

import java.awt.Color;

import lessons.recursion.hanoi.operations.HanoiMove;
import lessons.recursion.hanoi.operations.HanoiOperation;
import lessons.sort.baseball.operations.BaseballOperation;
import lessons.sort.baseball.operations.MoveOperation;
import lessons.sort.dutchflag.operations.DutchFlagOperation;
import lessons.sort.dutchflag.operations.DutchFlagSwap;
import lessons.sort.pancake.universe.operations.FlipOperation;
import lessons.sort.pancake.universe.operations.PancakeOperation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import plm.universe.GridWorldCellOperation;
import plm.universe.Operation;
import plm.universe.bat.BatOperation;
import plm.universe.bat.BatTest;
import plm.universe.bugglequest.BuggleOperation;
import plm.universe.bugglequest.BuggleWorldCellOperation;
import plm.universe.bugglequest.ChangeBuggleBrushDown;
import plm.universe.bugglequest.ChangeBuggleCarryBaggle;
import plm.universe.bugglequest.ChangeBuggleDirection;
import plm.universe.bugglequest.ChangeCellColor;
import plm.universe.bugglequest.ChangeCellContent;
import plm.universe.bugglequest.ChangeCellHasBaggle;
import plm.universe.bugglequest.ChangeCellHasContent;
import plm.universe.bugglequest.MoveBuggleOperation;
import plm.universe.sort.operations.CopyOperation;
import plm.universe.sort.operations.CountOperation;
import plm.universe.sort.operations.GetValueOperation;
import plm.universe.sort.operations.SetValOperation;
import plm.universe.sort.operations.SortOperation;
import plm.universe.sort.operations.SwapOperation;
import plm.universe.turtles.operations.AddCircle;
import plm.universe.turtles.operations.AddLine;
import plm.universe.turtles.operations.AddSizeHint;
import plm.universe.turtles.operations.ChangeTurtleVisible;
import plm.universe.turtles.operations.ClearCanvas;
import plm.universe.turtles.operations.MoveTurtle;
import plm.universe.turtles.operations.RotateTurtle;
import plm.universe.turtles.operations.TurtleOperation;

/**
 * The {@link Operation} to {@link JSONObject} conversion tool.
 * @author Tanguy
 *
 */
@SuppressWarnings("unchecked")
public abstract class OperationParser {
// Entry point
	/**
	 * The {@link OperationParser} entry point.
	 * @param operation {@link Operation} to give to the OperationParser.
	 * @return the result {@link JSONObject}.
	 */
	public static JSONObject toJSON(Operation operation) {
		return Router.toJSON(operation);
	}
	
	public static JSONArray colorWrapper(Color color) {
		JSONArray json = new JSONArray();
		json.add(color.getRed());
		json.add(color.getGreen());
		json.add(color.getBlue());
		json.add(color.getAlpha());
		return json;
	}
	
// Operation routing
	/**
	 * The {@link Operation} parser.
	 * @author Tanguy
	 *
	 */
	private static class Router {
		public static JSONObject toJSON(Operation o) {
			JSONObject r;
			if(o instanceof BaseballOperation)
				r = Baseball.toJSON((BaseballOperation) o);
			else if(o instanceof BatOperation)
				r = Bat.toJSON((BatOperation) o);
			else if(o instanceof BuggleOperation)
				r = Buggle.toJSON((BuggleOperation) o);
			else if(o instanceof DutchFlagOperation)
				r = DutchFlag.toJSON((DutchFlagOperation) o);
			else if(o instanceof GridWorldCellOperation)
				r = toJSON((GridWorldCellOperation) o);
			else if(o instanceof HanoiOperation)
				r = Hanoi.toJSON((HanoiOperation) o);
			else if(o instanceof PancakeOperation)
				r = Pancake.toJSON((PancakeOperation) o);
			else if(o instanceof SortOperation)
				r = Sort.toJSON((SortOperation) o);
			else if(o instanceof TurtleOperation)
				r = Turtle.toJSON((TurtleOperation) o);
			else r = new JSONObject();
			r.put("type", o.getName());
			r.put("msg", o.getMsg());
			return r;
		}
// Baseball operations
		private static class Baseball {
			private static JSONObject toJSON(BaseballOperation o) {
				JSONObject res;
				if(o instanceof MoveOperation)
					res = toJSON((MoveOperation) o);
				else
					res = new JSONObject();
				res.put("baseballID", o.getEntity().getName());
				return res;
			}
			
			private static JSONObject toJSON(MoveOperation o) {
				JSONObject res = new JSONObject();
				res.put("base", o.getBase());
				res.put("position", o.getPosition());
				res.put("oldBase", o.getOldBase());
				res.put("oldPosition", o.getOldPosition());
				return res;
			}
		}
// Bat operations
		private static class Bat {
			private static JSONObject toJSON(BatOperation o) {
				JSONObject res = new JSONObject();
				JSONArray resArray = new JSONArray();
				for(Object t : o.getBatWorld().getTests().toArray()) {
					JSONObject resElem = new JSONObject();
					resElem.put("test", ((BatTest) t).formatAsString());
					resElem.put("answered", ((BatTest) t).isAnswered());
					resElem.put("correct", ((BatTest) t).isCorrect());
					resElem.put("visible", ((BatTest) t).isVisible());
					resArray.add(resElem);
				}
				res.put("type", "BatWorld");
				res.put("batTests", resArray);
				return res;
			}
		}
// BuggleWorldCell operations
		/**
		 * The {@link BuggleWorldCell} parser.
		 * @author Tanguy
		 *
		 */
		private static class BuggleWorldCell {
			private static JSONObject toJSON(BuggleWorldCellOperation o) {
				JSONObject r;
				if(o instanceof ChangeCellColor)
					r = toJSON((ChangeCellColor) o);
				else if(o instanceof ChangeCellHasBaggle)
					r = toJSON((ChangeCellHasBaggle) o);
				else if(o instanceof ChangeCellHasContent)
					r = toJSON((ChangeCellHasContent) o);
				else if(o instanceof ChangeCellContent)
					r = toJSON((ChangeCellContent) o);
				else
					r = new JSONObject();
				return r;
			}
			private static JSONObject toJSON(ChangeCellColor o) {
				JSONObject res = new JSONObject();
				res.put("oldColor", colorWrapper(o.getOldColor()));
				res.put("newColor", colorWrapper(o.getNewColor()));
				res.put("operation", "ChangeCellColor");
				return res;
			}
			private static JSONObject toJSON(ChangeCellHasBaggle o) {
				JSONObject res = new JSONObject();
				res.put("oldHasBaggle", o.getOldHasBaggle());
				res.put("newHasBaggle", o.getNewHasBaggle());
				res.put("operation", "ChangeCellHasBaggle");
				return res;
			}
			private static JSONObject toJSON(ChangeCellHasContent o) {
				JSONObject res = new JSONObject();
				res.put("oldHasContent", o.getOldHasContent());
				res.put("newHasContent", o.getNewHasContent());
				res.put("operation", "ChangeCellHasContent");
				return res;
			}
			private static JSONObject toJSON(ChangeCellContent o) {
				JSONObject res = new JSONObject();
				res.put("oldContent", o.getOldContent());
				res.put("newContent", o.getNewContent());
				res.put("operation", "ChangeCellContent");
				return res;
			}
		}
		
// Buggle operations
		/**
		 * The {@link BuggleOperation} parser.
		 * @author Tanguy
		 *
		 */
		private static class Buggle {
			private static JSONObject toJSON(BuggleOperation o) {
				JSONObject r;
				if(o instanceof MoveBuggleOperation)
					r = toJSON((MoveBuggleOperation) o);
				else if(o instanceof ChangeBuggleCarryBaggle)
					r = toJSON((ChangeBuggleCarryBaggle) o);
				else if(o instanceof ChangeBuggleBrushDown)
					r = toJSON((ChangeBuggleBrushDown) o);
				else if(o instanceof ChangeBuggleDirection)
					r = toJSON((ChangeBuggleDirection) o);
				else
					r = new JSONObject();
				r.put("buggleID", o.getBuggle().getName());
				return r;
			}
			private static JSONObject toJSON(MoveBuggleOperation o) {
				JSONObject res = new JSONObject();
				res.put("oldX", o.getOldX());
				res.put("oldY", o.getOldY());
				res.put("newX", o.getNewX());
				res.put("newY", o.getNewY());
				res.put("operation", "MoveBuggleOperation");
				return res;
			}
			private static JSONObject toJSON(ChangeBuggleCarryBaggle o) {
				JSONObject res = new JSONObject();
				res.put("oldCarryBaggle", o.getOldCarryBaggle());
				res.put("newCarryBaggle", o.getNewCarryBaggle());
				res.put("operation", "ChangeBuggleCarryBaggle");
				return res;
			}
			private static JSONObject toJSON(ChangeBuggleBrushDown o) {
				JSONObject res = new JSONObject();
				res.put("oldBrushDown", o.getOldBrushDown());
				res.put("newBrushDown", o.getNewBrushDown());
				res.put("operation", "ChangeBuggleBrushDown");
				return res;
			}
			private static JSONObject toJSON(ChangeBuggleDirection o) {
				JSONObject res = new JSONObject();
				res.put("oldDirection", o.getOldDirection().intValue());
				res.put("newDirection", o.getNewDirection().intValue());
				res.put("operation", "ChangeBuggleDirection");
				return res;
			}
		}
// DutchFlag operations
		private static class DutchFlag {
			private static JSONObject toJSON(DutchFlagOperation o) {
				JSONObject res;
				if(o instanceof DutchFlagSwap)
					res = toJSON((DutchFlagSwap) o);
				else
					res = new JSONObject();
				res.put("dutchFlagID", o.getEntity().getName());
				return res;
			}
			
			private static JSONObject toJSON(DutchFlagSwap o) {
				JSONObject res = new JSONObject();
				res.put("destination", o.getDestination());
				res.put("source", o.getSource());
				return res;
			}
		}
// GridWorldCell operation
		private static JSONObject toJSON(GridWorldCellOperation o) {
			JSONObject r;
			if(o instanceof BuggleWorldCellOperation)
				r = BuggleWorldCell.toJSON((BuggleWorldCellOperation) o);
			else r = new JSONObject();
			JSONObject cell = new JSONObject();
			cell.put("x", o.getCell().getX());
			cell.put("y", o.getCell().getY());
			r.put("cell", cell);
			return r;
		}
// Hanoi operation
		private static class Hanoi {
			private static JSONObject toJSON(HanoiOperation o) {
				JSONObject res;
				if(o instanceof HanoiMove)
					res = toJSON((HanoiMove) o);
				else
					res = new JSONObject();
				res.put("hanoiID", o.getEntity().getName());
				return res;
			}
			
			private static JSONObject toJSON(HanoiMove o) {
				JSONObject res = new JSONObject();
				res.put("source", o.getSource());
				res.put("destination", o.getDestination());
				return res;
			}
		}
// Pancake operation
		private static class Pancake {
			private static JSONObject toJSON(PancakeOperation o) {
				JSONObject res;
				if(o instanceof FlipOperation)
					res = toJSON((FlipOperation) o);
				else
					res = new JSONObject();
				res.put("pancakeID", o.getEntity().getName());
				return res;
			}
			private static JSONObject toJSON(FlipOperation o) {
				JSONObject res = new JSONObject();
				res.put("number", o.getNumber());
				return res;
			}
		}
// Sort operations
		private static class Sort {
			private static JSONObject toJSON(SortOperation o) {
				JSONObject res;
				if(o instanceof SetValOperation)
					res = toJSON((SetValOperation) o);
				else if(o instanceof SwapOperation)
					res = toJSON((SwapOperation) o);
				else if(o instanceof CopyOperation)
					res = toJSON((CopyOperation) o);
				else if(o instanceof CountOperation)
					res = toJSON((CountOperation) o);
				else if(o instanceof GetValueOperation)
					res = toJSON((GetValueOperation) o);
				else res = new JSONObject();
				res.put("sortID", o.getEntity().getName());
				return res;
			}
			
			private static JSONObject toJSON(SetValOperation o) {
				JSONObject res = new JSONObject();
				res.put("value", o.getValue());
				res.put("oldValue",  o.getOldValue());
				res.put("position", o.getPosition());
				return res;
			}
			
			private static JSONObject toJSON(SwapOperation o) {
				JSONObject res = new JSONObject();
				res.put("destination", o.getDestination());
				res.put("source", o.getSource());
				return res;
			}
			
			private static JSONObject toJSON(CopyOperation o) {
				JSONObject res = new JSONObject();
				res.put("destination", o.getDestination());
				res.put("source", o.getSource());
				res.put("oldValue", o.getOldValue());
				return res;
			}
			
			private static JSONObject toJSON(CountOperation o) {
				JSONObject res = new JSONObject();
				res.put("read", o.getRead());
				res.put("write", o.getWrite());
				res.put("oldRead", o.getOldRead());
				res.put("oldWrite", o.getOldWrite());
				return res;
			}
			
			private static JSONObject toJSON(GetValueOperation o) {
				JSONObject res = new JSONObject();
				res.put("position", o.getPosition());
				return res;
			}
		}
		
		private static class Turtle {
			private static JSONObject toJSON(TurtleOperation o) {
				JSONObject res;
				if(o instanceof AddCircle)
					res = toJSON((AddCircle) o);
				else if(o instanceof AddLine)
					res = toJSON((AddLine) o);
				else if(o instanceof AddSizeHint)
					res = toJSON((AddSizeHint) o);
				else if(o instanceof ChangeTurtleVisible)
					res = toJSON((ChangeTurtleVisible) o);
				else if(o instanceof ClearCanvas)
					res = toJSON((ClearCanvas) o);
				else if(o instanceof MoveTurtle)
					res = toJSON((MoveTurtle) o);
				else if(o instanceof RotateTurtle)
					res = toJSON((RotateTurtle) o);
				else res = new JSONObject();
				res.put("turtleID", o.getTurtle().getName());
				return res;
			}
			
			private static JSONObject toJSON(AddCircle o) {
				JSONObject res = new JSONObject();
				res.put("x", o.getX());
				res.put("y",  o.getY());
				res.put("radius", o.getRadius());
				res.put("color", colorWrapper(o.getColor()));
				return res;
			}
			
			private static JSONObject toJSON(AddLine o) {
				JSONObject res = new JSONObject();
				res.put("x1", o.getX1());
				res.put("y1",  o.getY1());
				res.put("x2", o.getX2());
				res.put("y2",  o.getY2());
				res.put("color", colorWrapper(o.getColor()));
				return res;
			}
			
			private static JSONObject toJSON(AddSizeHint o) {
				JSONObject res = new JSONObject();
				res.put("x1", o.getX1());
				res.put("y1",  o.getY1());
				res.put("x2", o.getX2());
				res.put("y2",  o.getY2());
				res.put("text", o.getText());
				return res;
			}
			
			private static JSONObject toJSON(ChangeTurtleVisible o) {
				JSONObject res = new JSONObject();
				res.put("oldVisible", o.getOldVisible());
				res.put("newVisible", o.getNewVisible());
				return res;
			}
			
			private static JSONObject toJSON(ClearCanvas o) {
				JSONObject res = new JSONObject();
				return res;
			}
			
			private static JSONObject toJSON(MoveTurtle o) {
				JSONObject res = new JSONObject();
				res.put("oldX", o.getOldX());
				res.put("oldY",  o.getOldY());
				res.put("newX", o.getNewX());
				res.put("newY", o.getNewY());
				return res;
			}
			
			private static JSONObject toJSON(RotateTurtle o) {
				JSONObject res = new JSONObject();
				res.put("oldHeading", o.getOldHeading());
				res.put("newHeading", o.getNewHeading());
				return res;
			}
		}
	}
}
