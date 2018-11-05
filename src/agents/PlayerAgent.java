package agents;

import jade.lang.acl.ACLMessage;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.SimpleBehaviour;

public class PlayerAgent extends Agent {
	
	private static class Position {
		public static GridPoint getPosition(String position) {
			switch (position) {
				case "B_SITE":
					return new GridPoint(7, 40);
				default:
					return null;
			}
		}
	}
	
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	
	protected GridPoint spawn;
	protected GridPoint goalPos;
	private int hp;
	private boolean isIGL;
	
	public PlayerAgent(ContinuousSpace<Object> space, Grid<Object> grid, GridPoint spawn, boolean isIGL) {
		this.space = space; this.grid = grid; this.spawn = spawn; this.isIGL = isIGL;
		this.hp = 100;
	}
	
	@Override
	public void setup() {
		System.out.println("Reporting in.");
		
		if (isIGL)
			addBehaviour(new DelegateStrategy());
		
		addBehaviour(new AliveBehaviour());
		addBehaviour(new WalkingBehaviour());
		addBehaviour(new ListeningBehaviour());
	}
	
	public GridPoint getSpawn() {
		return spawn;
	}

	public void setSpawn(GridPoint spawn) {
		this.spawn = spawn;
	}

	@Override
	public void takeDown() {
		System.out.println("I've been killed.");
	}
	
	public void checkSurroundings() {
	}
	
	private class DelegateStrategy extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			ACLMessage stratMsg = new ACLMessage(ACLMessage.INFORM);
			stratMsg.setContent("B_SITE");
			
			for (int i = 0; i < 5; i++)
				stratMsg.addReceiver(new AID("CT" + i + "@AIAD Source", true));
			
			send(stratMsg);
		}

		@Override
		public boolean done() {
			return false;
		}
	}
	
	private class AliveBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			if (hp <= 0) takeDown();
		}
	}
	
	private class WalkingBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			if (goalPos != null)
				moveTowards(goalPos);	
		}
		
		@Override
		public int onEnd() {
			return 0;
		}
	}
	
	private class ListeningBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			//checkSurroundings();
			ACLMessage msg = receive();
			
			if (msg != null) {
				System.out.println(msg);
				goalPos = Position.getPosition(msg.getContent());
				//ACLMessage reply = msg.createReply();
				//reply.setPerformative(ACLMessage.INFORM);
				//reply.setContent("pong");
				//reply.addReceiver(msg.getSender());
				//send(reply);
			} else {
				block();				
			}
			
		}
		
	}
	
	public void moveTowards(GridPoint pt) {
		if (pt.equals(this.grid.getLocation(this))) return;
		
		NdPoint posStart = space.getLocation(this);
		NdPoint posEnd = new NdPoint(pt.getX(), pt.getY());
		space.moveByVector(this, 1, SpatialMath.calcAngleFor2DMovement(space, posStart, posEnd), 0);
		
		NdPoint posRes = space.getLocation(this);
		grid.moveTo(this, (int) posRes.getX(), (int) posRes.getY());
	}
}