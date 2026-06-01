package model;

/**
 *
 * @author adotal
 */
public class FriendRequest extends Request {

    private int id;
    private User senderUser;
    private User targetUser;

    public FriendRequest() {
        super();
    }

    public FriendRequest(int id, User senderUser, User targetUser, RequestStatus status) {
        super(status);
        this.id = id;
        this.senderUser = senderUser;
        this.targetUser = targetUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getSenderUser() {
        return senderUser;
    }

    public void setSenderUser(User senderUser) {
        this.senderUser = senderUser;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

}
