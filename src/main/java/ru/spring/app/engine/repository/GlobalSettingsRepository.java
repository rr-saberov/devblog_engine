package ru.spring.app.engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.spring.app.engine.entity.GlobalSettings;

import javax.transaction.Transactional;

@Repository
public interface GlobalSettingsRepository extends JpaRepository<GlobalSettings, Long> {

    @Query("SELECT gs FROM GlobalSettings gs WHERE gs.code = 'MULTIUSER_MODE'")
    GlobalSettings getMultiuserMode();

    @Transactional
    @Modifying
    @Query("UPDATE GlobalSettings settings SET settings.value = :multiuser WHERE settings.code = 'MULTIUSER_MODE'")
    void updateMultiUserMode(@Param("multiuser") String multiuserMode);

    @Transactional
    @Modifying
    @Query("UPDATE GlobalSettings settings SET settings.value = :post_premoderation WHERE settings.code = 'POST_MODERATION'")
    void updatePostPreModeration(@Param("post_premoderation") String postPreModeration);

    @Transactional
    @Modifying
    @Query("UPDATE GlobalSettings settings SET settings.value = :statistic WHERE settings.code = 'STATISTICS_IS_PUBLIC'")
    void updateStatisticIsPublic(@Param("statistic") String statisticIsPublic);
}
