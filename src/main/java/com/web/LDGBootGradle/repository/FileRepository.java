package com.web.LDGBootGradle.repository;

import com.web.LDGBootGradle.model.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<UploadFile, Long> {

    /*
    @EntityGraph : fetchType 무시하고 board를 sql을 호출
    n+1 sql문제로 성능관련 이슈가 있다면 EntityGraph 이용해서 조인시켜 쿼리날리자

    @EntityGraph(attributePaths = { "boards" })
    List<User> findAll();

    User findByUsername(String username);
    */

    UploadFile findByUploadFileId(Long id);


}
