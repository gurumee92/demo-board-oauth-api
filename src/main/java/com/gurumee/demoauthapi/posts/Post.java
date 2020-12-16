package com.gurumee.demoauthapi.posts;

import com.gurumee.demoauthapi.accounts.Account;
import com.gurumee.demoauthapi.common.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter @ToString @EqualsAndHashCode(of = "id")
@Builder
public class Post extends BaseTimeEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account owner;
}
