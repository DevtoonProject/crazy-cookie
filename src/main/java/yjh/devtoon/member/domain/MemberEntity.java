package yjh.devtoon.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yjh.devtoon.common.entity.BaseEntity;
import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "member")
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_no", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "membership_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipStatus membershipStatus;

    @ManyToMany
    @JoinTable(
            name = "member_authority",
            joinColumns = {@JoinColumn(name = "member_no", referencedColumnName = "member_no")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
    private Set<Authority> authorities;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    @Builder
    public MemberEntity(
            final Long id,
            final String name,
            final String email,
            final String password,
            final MembershipStatus membershipStatus,
            final Set<Authority> authorities,
            final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.membershipStatus = membershipStatus;
        this.authorities = authorities;
        this.deletedAt = deletedAt;
    }

    public static MemberEntity create(
            final String name,
            final String email,
            final String password,
            final MembershipStatus membershipStatus,
            final Set<Authority> authorities
    ) {
        MemberEntity member = new MemberEntity();
        member.name = name;
        member.email = email;
        member.password = password;
        member.membershipStatus = membershipStatus;
        member.authorities = authorities;
        return member;
    }

    public void change(final MembershipStatus membershipStatus) {
        this.membershipStatus = membershipStatus;
    }


    public boolean isPremium() {
        return membershipStatus == MembershipStatus.PREMIUM;
    }

    @Override
    public String toString() {
        return "MemberEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", membershipStatus=" + membershipStatus +
                ", authorities=" + authorities +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
