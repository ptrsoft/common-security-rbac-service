package com.synectiks.security.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.synectiks.security.config.IDBConsts;
import com.synectiks.security.domain.PSqlEntity;

import javax.persistence.*;

/**
 * @author Manoj
 */
@Entity
@Table(name = IDBConsts.Tbl_EmailQueue)
public class EmailQueue extends PSqlEntity {
	private static final long serialVersionUID = 9158287363589907319L;

    @Column(nullable = true)
    private String status;

    @Column(name = "mail_type", nullable = true)
    private String mailType;

    @Column(name = "mail_from", nullable = true)
    private String mailFrom;

    @Column(name = "mail_to", nullable = true)
    private String mailTo;

    @Column(name = "mail_subject", nullable = true)
    private String mailSubject;

    @Column(name = "mail_body", nullable = true)
    private String mailBody;

    @Column(name = "mail_template", nullable = true)
    private String mailTemplate;

    @Column(name = "user_name", nullable = true)
    private String userName;


    @OneToOne(targetEntity = Organization.class, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = "organizations", allowSetters = true)
    private Organization organization;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMailType() {
        return mailType;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }

    public String getMailTemplate() {
        return mailTemplate;
    }

    public void setMailTemplate(String mailTemplate) {
        this.mailTemplate = mailTemplate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public String toString() {
        return "EmailQueue{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", mailType='" + mailType + '\'' +
                ", mailFrom='" + mailFrom + '\'' +
                ", mailTo='" + mailTo + '\'' +
                ", mailSubject='" + mailSubject + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
