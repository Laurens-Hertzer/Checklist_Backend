package ch.bbw.lah.checklist_backend.repository;

import ch.bbw.lah.checklist_backend.model.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
}
