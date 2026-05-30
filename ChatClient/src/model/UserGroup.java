package model;

/**
 *
 * @author adotal
 */
// An UserGroup is an uset with a GroupInivitation
public class UserGroup extends User {

    private GroupInvitation invitation;

    public UserGroup() {
        super();
        // Penging default status for incitation
        this.invitation = new GroupInvitation();
    }

    public UserGroup(GroupInvitation invitation, int id, String userName, String email, boolean isConnected) {
        super(id, userName, email, isConnected);
        // Penging default status fir incitation
        this.invitation = invitation;
    }

    public GroupInvitation getInvitation() {
        return invitation;
    }

    public void setInvitation(GroupInvitation invitation) {
        this.invitation = invitation;
    }

}
