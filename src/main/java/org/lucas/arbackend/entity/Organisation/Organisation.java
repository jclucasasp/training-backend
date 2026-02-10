package org.lucas.arbackend.entity.Organisation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.lucas.arbackend.entity.BaseEntity;
import org.lucas.arbackend.entity.security.Role;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NamedEntityGraph(name = "Organisation.withDetails",
attributeNodes = {
        @NamedAttributeNode(value = "profile"),
        @NamedAttributeNode(value = "subscription"),
        @NamedAttributeNode(value = "profile", subgraph = "profile-subgraph")
}, subgraphs = {
        @NamedSubgraph(name = "profile-subgraph",
                attributeNodes = {
                        @NamedAttributeNode("address"),
                        @NamedAttributeNode("apiKey")
                }),
        @NamedSubgraph(name = "subscription",
                attributeNodes = {
                        @NamedAttributeNode("subscriptionPlan")
                })
})
@Table(name = "organisation")
@Getter @Setter
@SQLDelete(sql = "UPDATE organisation SET ended_at = CURRENT_TIMESTAMP WHERE org_id = ?")
@SQLRestriction("ended_at IS NULL")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Organisation extends BaseEntity {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "org_id")
    private Long id;

    @Column(name = "org_email", unique = true, nullable = false)
    private String email;

    @Column(name = "org_password", nullable = false)
    private String password;

    // mappedBy tells Hibernate: "I am the inverse side; the other table owns the column."
    // @JoinColumn tells Hibernate: "I own the column in my table."

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "org_role_id")
    private Role role;

    @OneToOne(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @OneToOne(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrganisationSubscription subscription;

}
