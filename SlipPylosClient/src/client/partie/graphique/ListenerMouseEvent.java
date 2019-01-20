package client.partie.graphique;

class ListenerMouseEvent {
	final public int button, mouseX, mouseY;
	final public ListenerMouseEventType eventType;
	
	public ListenerMouseEvent(int arg_button, int arg_mouseX, int arg_mouseY, ListenerMouseEventType arg_eventType) {
		button = arg_button;
		mouseX = arg_mouseX;
		mouseY = arg_mouseY;
		eventType = arg_eventType;
	}
	
}
