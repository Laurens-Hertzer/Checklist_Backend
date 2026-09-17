package ch.bbw.lah.checklist_backend.repository;

import ch.bbw.lah.checklist_backend.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    List<Setting> findBySettingId(Long settingId);
}
