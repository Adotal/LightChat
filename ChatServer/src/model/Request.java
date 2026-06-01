package model;

/**
 *
 * @author adotal
 */

/*
    Intended for
        -friend requests
        -group invitations
 */
public abstract class Request {

    public enum RequestStatus {
        APPROVED,
        PENDING,
        DENIED,
    }

    private RequestStatus status = RequestStatus.PENDING;

    public Request() {
        this.status = RequestStatus.PENDING;
    }

    public Request(RequestStatus status) {
        this.status = status;
    }

    public void accept() {
        status = RequestStatus.APPROVED;
    }

    public void deny() {
        status = RequestStatus.DENIED;
    }

    public RequestStatus getStatus() {
        return this.status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

}
