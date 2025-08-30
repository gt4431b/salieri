package bill.zeacc.salieri.fourthgraph;


public final class ChatMsg implements java.io.Serializable {
    private static final long serialVersionUID = -8936140400377593199L ;
	public enum Role { SYSTEM, USER, ASSISTANT }
    private final Role role;
    private final String content;

    public ChatMsg(Role role, String content) {
        this.role = role;
        this.content = content;
    }
    public Role role() { return role; }
    public String content() { return content; }
}
