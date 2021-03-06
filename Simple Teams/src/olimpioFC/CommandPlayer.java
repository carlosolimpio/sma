package olimpioFC;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import simple_soccer_lib.PlayerCommander;
import simple_soccer_lib.perception.FieldPerception;
import simple_soccer_lib.perception.MatchPerception;
import simple_soccer_lib.perception.PlayerPerception;
import simple_soccer_lib.utils.EFieldSide;
import simple_soccer_lib.utils.Vector2D;

public class CommandPlayer extends Thread {
	private int LOOP_INTERVAL = 10;
	private PlayerCommander commander;
	private PlayerPerception selfPerc;
	private FieldPerception fieldPerc;
	private MatchPerception matchPerc;
	
	public CommandPlayer(PlayerCommander player) {
		commander = player;
	}
	
	@Override
	public void run() {
		System.out.println("Executando...");
		long nextIteration = System.currentTimeMillis() + LOOP_INTERVAL;
		updatePerceptions();
		
		switch(selfPerc.getUniformNumber()) {
		case 1:
			acaoGoleiro(nextIteration);
			break;
		case 2:
			acaoArmador(nextIteration, -20);
			break;
		case 3:
			acaoArmador(nextIteration, 0);
			break;
		case 4:
			acaoArmador(nextIteration, 20);
			break;
		case 5:
			acaoAtacante(nextIteration, -15);
			break;
		case 6:
			acaoAtacante(nextIteration, 0);
			break;
		case 7:
			acaoAtacante(nextIteration, 15);
			break;
		default:
			break;
		}
		
	}
	
	private void acaoGoleiro(long nextIteration) {
		double xInit = -48, yInit = 0, ballX = 0, ballY = 0;
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos = new Vector2D(xInit*side.value(), yInit*side.value());
		Vector2D ballPos;
		Rectangle area = side == EFieldSide.LEFT ? new Rectangle(-52, -20, 16, 40) : new Rectangle(36, -20, 16, 40);
		
		while(true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			
			switch(matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(xInit, yInit);		// posiciona
				break;
			case PLAY_ON:
				ballX = fieldPerc.getBall().getPosition().getX();
				ballY = fieldPerc.getBall().getPosition().getY();
				
				posiciona(ballPos); //add side
				
				if(isPointsAreClose(selfPerc.getPosition(), ballPos, 1)){
					kickToPoint(new Vector2D(0,0), 100);		// chutar
				} else if(area.contains(ballX, ballY)) {
					dash(ballPos);		// defender
				} else if(!isPointsAreClose(selfPerc.getPosition(), initPos, 3)){// recuar
					dash(initPos);
				} else {
					// olhar para a bola
					turnToPoint(ballPos);
				}
				break;
				/* Todos os estados da partida */
				default:
					break;
			}
		}
	}
	
	private void acaoArmador(long nextIteration, int pos) {
		double xInit=-30, yInit=pos;
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos = new Vector2D(xInit*side.value(), yInit);
		Vector2D goalPos = new Vector2D(50*side.value(), 0);
		Vector2D ballPos, vTemp;
		PlayerPerception pTemp;
		
		while(true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			
			switch(matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(xInit, yInit);
				break;
			case PLAY_ON:
				if(isPointsAreClose(selfPerc.getPosition(), ballPos, 1)) {

				}
			break;
				
				/*Todos os estados da partida */
				
			default:
				break;
			}
		}
	}
	
	private void acaoAtacante(long nextIteration, int pos) {
		double xInit=-8, yInit=pos;
		EFieldSide side = selfPerc.getSide();
		Vector2D initPos = new Vector2D(xInit*side.value(), yInit);
		Vector2D goalPos = new Vector2D(50*side.value(), 0);
		Vector2D ballPos;
		PlayerPerception pTemp;
		
		while(true) {
			updatePerceptions();
			ballPos = fieldPerc.getBall().getPosition();
			
			switch(matchPerc.getState()) {
			case BEFORE_KICK_OFF:
				commander.doMoveBlocking(xInit, yInit);
				break;
			case PLAY_ON:
				if(isPointsAreClose(selfPerc.getPosition(), ballPos, 1)) {
					if(isPointsAreClose(ballPos, goalPos, 30))
						kickToPoint(goalPos, 100); //chuta pro gol
					else
						kickToPoint(goalPos, 25); //conduz pro gol
				} else {
					pTemp = getClosestPlayerPoint(ballPos, side, 3);
					if(pTemp != null && pTemp.getUniformNumber() == selfPerc.getUniformNumber())
						dash(ballPos); //pega bola
					else if(!isPointsAreClose(selfPerc.getPosition(), initPos, 3))
						dash(initPos); //recua
					else
						turnToPoint(ballPos); //olha pra bola
				}
			break;
				
				/*Todos os estados da partida */
				
			default:
				break;
			}
		}
		
	}
	
	private PlayerPerception getClosestPlayerPoint(Vector2D point, EFieldSide side, double margin) {
		ArrayList<PlayerPerception> lp = fieldPerc.getTeamPlayers(side);
		
		PlayerPerception np = null;
		
		if(lp != null && !lp.isEmpty()) {
			double dist, temp;
			dist = lp.get(0).getPosition().distanceTo(point);
			np = lp.get(0);
			
			if(isPointsAreClose(np.getPosition(), point, margin))
				return np;
			for(PlayerPerception p : lp) {
				if(p.getPosition() == null) break;
				if(isPointsAreClose(p.getPosition(), point, margin))
					return p;
				temp = p.getPosition().distanceTo(point);
				if(temp < dist) {
					dist = temp;
					np = p;
				}
			}
		}
		return np;
	}
	
	private void dash(Vector2D point) {
		if(selfPerc.getPosition().distanceTo(point) <= 1)
			return;
		if(!isAlignToPoint(point, 15))
			turnToPoint(point);
		commander.doDashBlocking(70);
	}
	
	private void posiciona(Vector2D point) {
		
		
		//if(!isAlignToPoint(point, 15))
		//	turnToPoint(point);
		
	}
	
	private void turnToPoint(Vector2D point) {
		Vector2D newDirection = point.sub(selfPerc.getPosition());
		commander.doTurnToDirectionBlocking(newDirection);
	}
	
	private boolean isPointsAreClose(Vector2D reference, Vector2D point, double margin) {
		return reference.distanceTo(point) <= margin;
	}
	
	private boolean isAlignToPoint(Vector2D point, double margin) {
		double angle = point.sub(selfPerc.getPosition()).angleFrom(selfPerc.getDirection());
		return angle < margin && angle > margin * (-1);
	}
	
	private void kickToPoint(Vector2D point, double intensity) {
		Vector2D newDirection = point.sub(selfPerc.getPosition());
		double angle = newDirection.angleFrom(selfPerc.getDirection());
		
		if(angle > 90 || angle < 90) {
			commander.doTurnToDirectionBlocking(newDirection);
			angle = 0;
		}
		commander.doKickBlocking(intensity, angle);
	}
	
	private void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelfBlocking();
		FieldPerception newField = commander.perceiveFieldBlocking();
		MatchPerception newMatch = commander.perceiveMatchBlocking();
		
		if (newSelf != null)
			this.selfPerc = newSelf;
		if (newField != null)
			this.fieldPerc = newField;
		if(newMatch != null)
			this.matchPerc = newMatch;
	}
}
