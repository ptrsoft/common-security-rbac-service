/**
 *
 */
package com.synectiks.security.repositories;

import com.synectiks.security.entities.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Manoj
 */
@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long>{

	 List<EmailQueue> findByCreatedBy(String createdBy);
     List<EmailQueue> findByUpdatedBy(String updatedBy);
     List<EmailQueue> findByStatus(String status);
     List<EmailQueue> findByMailSubject(String mailSubject);
     List<EmailQueue> findByMailTo(String mailTo);
     List<EmailQueue> findByMailFrom(String mailFrom);
     List<EmailQueue> findByMailType(String mailType);

}
