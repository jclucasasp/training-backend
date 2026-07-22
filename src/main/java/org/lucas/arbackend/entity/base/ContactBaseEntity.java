package org.lucas.arbackend.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

// Change this to @Embedded when needing to save a primary and secondary contact

/*
@EmbeddedCoumnNaming("primary_")
@EmbeddedCoumnNaming("secondary_")

How to override the attributes names inside your entity
@Embedded
@AttributeOverrides({
  @AttributeOverride( name = "firstName", column = @Column(name = "contact_first_name")),
  @AttributeOverride( name = "lastName", column = @Column(name = "contact_last_name")),
  @AttributeOverride( name = "phone", column = @Column(name = "contact_phone"))
})
private ContactPerson contactPerson;
 */
@MappedSuperclass
@Getter @Setter
public abstract class ContactBaseEntity extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email()
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;
}
