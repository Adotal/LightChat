package model;

/**
 *
 * @author adotal
 */
public class GroupInvitation extends Request {

    private int id;
    private Group group;
    // GroupOwner is needed to print "JohnDoe has invited you to HappyGroup"
    private User groupOwnerUser;
    private User invitedUser;

    public GroupInvitation() {
        super();
    }

    public GroupInvitation(int id, Group group, User groupOwnerUser, User invitedUser, RequestStatus status) {
        super(status);
        this.id = id;
        this.group = group;
        this.groupOwnerUser = groupOwnerUser;
        this.invitedUser = invitedUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getGroupOwnerUser() {
        return groupOwnerUser;
    }

    public void setGroupOwnerUser(User groupOwnerUser) {
        this.groupOwnerUser = groupOwnerUser;
    }

    public User getInvitedUser() {
        return invitedUser;
    }

    public void setInvitedUser(User invitedUser) {
        this.invitedUser = invitedUser;
    }

    public String getInvitationMessage() {
        return groupOwnerUser.getName() + " te invitó a " + group.getTitle();
    }

}
