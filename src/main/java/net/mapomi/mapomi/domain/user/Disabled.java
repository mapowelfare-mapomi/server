package net.mapomi.mapomi.domain.user;

import lombok.Getter;
import lombok.Setter;
import net.mapomi.mapomi.common.BaseTimeEntity;
import net.mapomi.mapomi.domain.Post;
import net.mapomi.mapomi.domain.Role;
import net.mapomi.mapomi.dto.request.JoinDto;

import javax.persistence.*;
import java.util.*;

@Getter
@Entity
@DiscriminatorValue("disabled")
public class Disabled extends User{

    @Column
    private int age;

    @Column
    private String residence=""; //거주지

    @Column
    private boolean certified =false;

    @Column
    private int popularity=0;

    @Column
    private String type;

    @OneToMany(mappedBy = "disabled",cascade = CascadeType.MERGE)
    private List<Post> posts = new ArrayList<>();

    @OneToOne(mappedBy = "disabled",cascade = CascadeType.MERGE)
    private Observer observer;


    public Disabled(JoinDto dto) {
        super(dto.getId(), dto.getPassword(), dto.getNickname(), "", Role.setRole("disabled"));
        this.age = dto.getAge();
        this.residence = dto.getResidence();
        this.type = dto.getType();
    }

    protected Disabled() {super();}
}
