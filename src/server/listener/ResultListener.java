package server.listener;

import java.io.IOException;

import org.xnap.commons.i18n.I18n;

import server.Main;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import plm.core.GameStateListener;
import plm.core.model.Game;
import plm.core.model.Game.GameState;
import plm.core.model.lesson.ExecutionProgress;
import plm.core.model.lesson.Exercise;
import server.parser.ReplyMsg;

public class ResultListener implements GameStateListener {

	private Game currGame;
	Channel channel;
	String sendTo;
	BasicProperties properties;
	
	public ResultListener(Channel c, String s) {
		channel = c;
		sendTo = s;
	}
	
	public void setProps(BasicProperties p) {
		properties = p;
	}
	
	public void setGame(Game g) {
		if(currGame != null)
			currGame.removeGameStateListener(this);
		currGame = g;
		g.addGameStateListener(this);
	}
	
	@Override
	public ResultListener clone() {
		ResultListener res = new ResultListener(channel, sendTo);
		res.setGame(currGame);
		return res;
	}

	@Override
	public void stateChanged(GameState state) {
		switch(state) {
			case DEMO_ENDED :
			case EXECUTION_ENDED :
				Exercise e = (Exercise) currGame.getCurrentLesson().getCurrentExercise();
				Main.askEndStreamMain();
				send(e.lastResult, currGame.i18n);
				Main.freeMain();
				break;
			default:
				break;
		}
	}
	
	public void send(ExecutionProgress exPro, I18n i18n) {
		ReplyMsg replyMsg = new ReplyMsg(exPro, i18n);
		String message = replyMsg.toJSON();
		try {
			channel.basicPublish("", sendTo, properties, message.getBytes("UTF-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println(" [D] Sent end comm. message (" + properties.getCorrelationId() + ")");
	}
}
