package org.lucas.arbackend.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

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
