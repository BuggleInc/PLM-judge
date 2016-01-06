package generator.parser;

import java.awt.Color;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import plm.universe.hanoi.HanoiWorld;
import plm.universe.baseball.BaseballWorld;
import plm.universe.dutchflag.DutchFlagWorld;
import plm.universe.pancake.PancakeWorld;
import plm.universe.Entity;
import plm.universe.GridWorld;
import plm.universe.GridWorldCell;
import plm.universe.World;
import plm.universe.bat.BatTest;
import plm.universe.bat.BatWorld;
import plm.universe.bugglequest.AbstractBuggle;
import plm.universe.bugglequest.BuggleWorld;
import plm.universe.bugglequest.BuggleWorldCell;
import plm.universe.sort.SortingWorld;

@SuppressWarnings("unchecked")
public class WorldParser {
	// Entry point
		/**
		 * The {@link WorldParser} entry point.
		 * @param world {@link World} to give to the WorldParser.
		 * @return the result {@link JSONObject}.
		 */
	public static JSONObject toJSON(World world) {
		return Router.toJSON(world);
	}
	
	private static class Router {
		public static JSONObject toJSON(World w) {
			JSONObject r;
			if(w instanceof BaseballWorld)
				r = toJSON((BaseballWorld) w);
			else if(w instanceof BatWorld)
				r = toJSON((BatWorld) w);
			else if(w instanceof DutchFlagWorld)
				r = toJSON((DutchFlagWorld) w);
			else if(w instanceof GridWorld)
				r = Grid.toJSON((GridWorld) w);
			else if(w instanceof HanoiWorld)
				r = toJSON((HanoiWorld) w);
			else if(w instanceof PancakeWorld)
				r = Pancake.toJSON((PancakeWorld) w);
			else if(w instanceof SortingWorld)
				r = toJSON((SortingWorld) w);
			else
				r = null;
			if(r != null) {
				JSONObject res = new JSONObject();
				res.put(w.getName(), r);
				return res;
			}
			return null;
		}
		public static JSONObject toJSON(BaseballWorld w) {
			JSONObject r = new JSONObject();
		     r.put("type","BaseballWorld");
		     r.put("field",w.getField());
    		 r.put("baseAmount",w.getBasesAmount());
			 r.put("posAmount",w.getPositionsAmount());
			 r.put("moveCount",w.getMoveCount());
			 r.put("holePos",w.getHolePosition());
			 r.put("holeBase",w.getHoleBase());
			 return r;
		}
		public static JSONObject toJSON(BatWorld w) {    
			JSONObject r = new JSONObject();
			JSONArray arr = new JSONArray();
		    for(BatTest t : w.getTests()){
		    	JSONObject rInt = new JSONObject();
		    	//rInt.put("test", t.formatAsString());
		    	rInt.put("answered", t.isAnswered());
		    	rInt.put("correct", t.isCorrect());
		    	rInt.put("visible", t.isVisible());
		        arr.add(rInt);
		    }
		    r.put("type", "BatWorld");
		    r.put("batTests", arr);
		    return r;
		}
		public static JSONObject toJSON(DutchFlagWorld w) {
			JSONObject r = new JSONObject();
			r.put("type", "DutchFlagWorld");
			r.put("content", w.getContent());
			r.put("moveCount", w.getMove());
			return r;
		}
		public static JSONObject toJSON(HanoiWorld w) {
			JSONObject r = new JSONObject();
			r.put("type", "HanoiWorld");
			r.put("moveCount", w.getMoveCount());
			JSONArray arr = new JSONArray();
			/*
			for(Vector<Integer> sLine : w.getSlot()){
				JSONArray arrInt = new JSONArray();
				for(Integer slot : sLine) {
					arrInt.add(slot.intValue());
				}
				arr.add(arrInt);
			}
			*/
			r.put("slotVal", arr);
			return r;
		}
		public static JSONObject toJSON(SortingWorld w) {
			JSONObject r = new JSONObject();
			r.put("type",  "SortingWorld");
			r.put("values", w.getValues());
			r.put("readCount", w.getReadCount());
			r.put("writeCount", w.getWriteCount());
			return r;
		}
	}
	private static class Grid {
		public static JSONObject toJSON(GridWorld w) {
			JSONObject r;
			if(w instanceof BuggleWorld)
				r = toJSON((BuggleWorld) w);
			else 
				return null;
			r.put("width", w.getWidth());
			r.put("height", w.getHeight());
			r.put("cells", toJSON(w.getCells()));
			r.put("entities", toJSON(w.getEntities()));
			return r;
		}
		private static JSONArray toJSON(List<Entity> entities) {
			JSONArray arr = new JSONArray();
			for(Entity e : entities) {
				arr.add(toJSON(e));
			}
			return arr;
		}
		private static JSONObject toJSON(Entity entity) {
			JSONObject r = null;
			if(entity instanceof AbstractBuggle) {
				AbstractBuggle e = (AbstractBuggle) entity;
				r = new JSONObject();
				r.put("x", e.getX());
				r.put("y", e.getY());
				JSONArray arr = new JSONArray();
				Color color = e.getBodyColor();
				arr.add(color.getRed());
				arr.add(color.getGreen());
				arr.add(color.getBlue());
				arr.add(color.getAlpha());
				r.put("color", arr);
				r.put("direction", e.getDirection().intValue());
				r.put("carryBaggle", e.isCarryingBaggle());
			}
			JSONObject res = new JSONObject();
			res.put(entity.getName(), r);
			return res;
		}
		private static JSONArray toJSON(GridWorldCell[][] cells) {
			JSONArray arr = new JSONArray();
			for(GridWorldCell[] c : cells) {
				JSONArray arrInt = new JSONArray();
				for(GridWorldCell cInt : c) {
					arrInt.add(toJSON(cInt));
				}
				arr.add(arrInt);
			}
			return arr;
		}
		private static JSONObject toJSON(GridWorldCell c) {
			JSONObject r;
			if(c instanceof BuggleWorldCell)
				r = toJSON((BuggleWorldCell) c);
			else
				r = new JSONObject();
			r.put("x", c.getX());
			r.put("y", c.getY());
			return r;
		}
		private static JSONObject toJSON(BuggleWorldCell c) {
			JSONObject r = new JSONObject();
			Color color = c.getColor();
			JSONArray arr = new JSONArray();
			arr.add(color.getRed());
			arr.add(color.getGreen());
			arr.add(color.getBlue());
			arr.add(color.getAlpha());
			r.put("color",arr);
			r.put("hasBaggle",c.hasBaggle());
			r.put("hasContent",c.hasContent());
			r.put("content",c.getContent());
			r.put("hasLeftWall",c.hasLeftWall());
			r.put("hasTopWall",c.hasTopWall());
			return r;
		}
		private static JSONObject toJSON(BuggleWorld w) {
			JSONObject r = new JSONObject();
			r.put("type", "BuggleWorld");
			return r;
		}
	}
	private static class Pancake {

		public static JSONObject toJSON(PancakeWorld w) {
			JSONObject r = new JSONObject();
			r.put("type",  "PancakeWorld");
			//r.put("pancakeStack", toJSON(w.getStack()));
			r.put("moveCount", w.getMoveCount());
			r.put("numberFlip", 0);
			r.put("oldNumber", 0);
			r.put("burnedWorld", w.isBurnedWorld());
			return r;
		}

		private static JSONArray toJSON(plm.universe.pancake.Pancake[] stack) {
			JSONArray arr = new JSONArray();
			for(plm.universe.pancake.Pancake p : stack) {
				JSONObject o = new JSONObject();
				o.put("radius", p.getRadius());
				o.put("upsideDown", p.isUpsideDown());
				arr.add(o);
			}
			return arr;
		}
	}
}
