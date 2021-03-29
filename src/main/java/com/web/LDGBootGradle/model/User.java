package com.web.LDGBootGradle.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private boolean enabled;

    private String realname;
    private String email;
    private String postcode;
    private String address;
    private String extraAddress;
    private String detailAddress;
    private String introduce;


    @JsonIgnore //호출 시 해당 데이터 제외하고 뿌려줌
    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

    /*
      orphanRemoval = 부모가없는 데이더는 다 지움)
      mappedBy = "user" = Board 클래스의 user변수를 끌어다 씀
      cascade = CascadeType.ALL,orphanRemoval = true)

      fetch = FetchType.EAGER :: 사용자 조회시 Board에 대한 데이터를 같이 가져옴
      fetch = FetchType.LAZY :: 사용자 조회시 Board에 대한 데이터를 같이 안가지고옴(필요할때 가져옴)
    */
    //@JsonIgnore //호출 시 해당 데이터 제외하고 뿌려줌
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Board> boards = new ArrayList<>();
}
