package com.tuos.Collab.collabuser;

import com.tuos.Collab.document.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@Entity(name = "CollabUser")
@Table(name = "collab_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "email_unique", columnNames = "email")
        })
@Transactional
public class CollabUser implements UserDetails {

    @Id
    @SequenceGenerator(name = "collabuser_sequence", sequenceName = "student_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_sequence")
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "email", nullable = false, columnDefinition = "TEXT")
    private String email;

    @Column(name = "password")
    private String password;

    @ManyToMany
    @JoinTable(
            name = "editable_document",
            joinColumns = @JoinColumn(name="collab_user_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    Set<Document> editableDocuments;

    @OneToMany(mappedBy = "author")
    private Set<Document> createdDocuments;

    @Transient
    Collection<? extends GrantedAuthority> authorities;

    public CollabUser(String name, String email, String password) {
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public CollabUser() {

    }


    @Override
    public String getUsername() {
        return this.name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Document> getCreatedDocuments() {
        return createdDocuments;
    }

    /*
    INTERFACE METHODS
     */
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        //make everyone ROLE_USER
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            //anonymous inner type
            public String getAuthority() {
                return "ROLE_USER";
            }
        };
        grantedAuthorities.add(grantedAuthority);
        return grantedAuthorities;
    }



    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void addEditableDocument(Document document) {
        editableDocuments.add(document);
    }
}
