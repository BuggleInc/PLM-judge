package generator;

import java.io.IOException;

import org.xnap.commons.i18n.I18n;

import server.Connector;
import server.Main;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import plm.core.GameStateListener;
import plm.core.model.Game;
import plm.core.model.Game.GameState;
import plm.core.model.lesson.ExecutionProgress;
import server.parser.ReplyMsg;

/**
 * The {@link GameStateListener} implementation. It reacts when the game state changes, notifying the {@link Main} class and the {@link BasicListener}.
 * @author Tanguy
 *
 */
public class ResultListener implements GameStateListener {

	private Game currGame;
	Channel channel;
	String sendTo;
	BasicProperties properties;
	GameGest parent;
	
	/**
	 * The {@link ResultListener} constructor.
	 * @param connector the used connector
	 * @param lstn The basic listener to activate for stream end.
	 */
	public ResultListener(Connector connector, GameGest gameGest) {
		this.channel = connector.cOut();
		this.sendTo = connector.cOutName();
		this.parent = gameGest;
	}
	
	private ResultListener(Channel c, String sTo, GameGest parent) {
		this.channel = c;
		this.sendTo = sTo;
		this.parent = parent;
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
		ResultListener copy = new ResultListener(channel, sendTo, parent);
		copy.setProps(properties);
		copy.setGame(currGame);
		return this;
	}

	@Override
	public void stateChanged(GameState state) {
		switch(state) {
			case DEMO_ENDED :
			case EXECUTION_ENDED :
				parent.sendStream();
				parent.free();
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
		Main.logger.log(0, "Sent end comm. message (" + properties.getCorrelationId() + ")");
	}
}
