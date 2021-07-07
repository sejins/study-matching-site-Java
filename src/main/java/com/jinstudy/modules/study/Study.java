package com.jinstudy.modules.study;

import com.jinstudy.modules.account.Account;
import com.jinstudy.modules.account.UserAccount;
import com.jinstudy.modules.tag.Tag;
import com.jinstudy.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NamedEntityGraph(name="Study.withAll", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("members"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name="Study.withTagsAndManagers",attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name="Study.withZonesAndManagers",attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name="Study.withManagers",attributeNodes = {
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name="Study.withMembers",attributeNodes = {
        @NamedAttributeNode("members")})
@NamedEntityGraph(name="Study.withMembersAndManagers", attributeNodes = {
        @NamedAttributeNode("members"),
        @NamedAttributeNode("managers")})
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdateDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    // 스터디 개설할때, 개설하면서 스터디 매니저로 등록.
    public void addManager(Account account) {
        this.managers.add(account);
    }

    public void addMember(Account account) {
        if(this.published && !this.closed && this.recruiting && !this.managers.contains(account) && !this.members.contains(account)){
            this.members.add(account);
        }
        else {
            throw new RuntimeException("스터디에 가입할 수 없습니다. 이미 가입한 스터디인지 또는 스터디가 가입 가능한 상태인지 확인하세요.");
        }
    }

    public void removeMember(Account account) {
        if(!this.closed && this.members.contains(account)){
            this.members.remove(account);
        }
        else{
            throw new RuntimeException("스터디를 탈퇴할 수 없습니다. 스터디가 이미 종료 되었거나, 스터디에 참여 중이 아닐 수 있습니다.");
        }
    }


    // 타임리프에서 조건을 위해서 사용
    public boolean isJoinable(UserAccount userAccount){
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting() && !this.members.contains(account) && !this.managers.contains(account);
    }

    // 타임리프에서 조건을 위해서 사용
    public boolean isMember(UserAccount userAccount){
        Account account = userAccount.getAccount();
        return this.members.contains(account);
    }

    // 타임리프에서 조건을 위해서 사용
    public boolean isManager(UserAccount userAccount){
        Account account = userAccount.getAccount();
        return this.managers.contains(account);
    }

    public boolean canPublish() {
        return !this.closed && !this.published;
    }

    public void publish() {
        if(canPublish()){
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        }
        else{ // 이 경우에는 뷰에서 폼을 표현하지 않는다. 그렇기 때문에 클라이언트는 일반적인 방법으로 스터디 개시를 할 수 없지만  악의적인 방법을 통해서 publish 요청을 할 수 있다.
            throw new RuntimeException("스터디를 공개할 수 없는 상태 입니다. 스터디를 이미 공개했거나 이미 종료된 스터디입니다."); //이를 방지하기 위해서 런타임 에러를 던진다.
        }
    }

    public String getImage(){
        return image == null ? "/images/default.jpg" : image;
    }

    public boolean canClose() {
        return this.published && !this.closed;
    }

    public void close() {
        if(canClose()){
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        }
        else{throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디 입니다.");} // 여기도 마찬가지!
    }


    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdateDateTime == null || this.recruitingUpdateDateTime.isBefore(LocalDateTime.now().minusHours(1));
        // 조건문 순서 주의해서 작성해야한다!
    }

    public boolean canStartRecruit() {
        return !this.closed && this.published && !this.recruiting;
    }

    public void startRecruit() {
        if(canStartRecruit()){
            this.recruiting = true;
            this.recruitingUpdateDateTime = LocalDateTime.now();
        }
        else{
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디가 개설이 되었는지, 또는 이미 인원 모집 중인지 확인 후 다시 시도하세요.");
        }
    }

    public boolean canStopRecruit() {
        return !this.closed && this.published && this.recruiting;
    }

    public void stopRecruit() {
        if(canStopRecruit()){
            this.recruiting = false;
            this.recruitingUpdateDateTime = LocalDateTime.now();
        }
        else{
            throw new RuntimeException("인원 모집을 중단할 수 없습니다. 스터디가 개설이 되었는지, 또는 이미 인원 모집이 중단되었는지 확인 후 다시 시도하세요.");
        }
    }

    public boolean isRemovable() {
        return !this.published; //TODO 모임을 했던 스터디는 삭제할 수 없다.
    }

    public String getEncodedPath(String path) {
            return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }


    public boolean isManagedBy(Account account) {
       return this.managers.contains(account);
    }

    public List<String> getManagersName(){
        return this.managers.stream().map(Account :: getNickname).collect(Collectors.toList());
    }
}
