package com.mamadou.sutura.admin;
import com.mamadou.sutura.admin.controller.AdminAccountsController;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.user.enums.*;
import com.mamadou.sutura.user.repository.UserRepository;
import com.mamadou.sutura.audit.repository.AuditLogRepository;
import com.mamadou.sutura.auth.service.RefreshTokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AdminAccountsTest {
 UserRepository users=mock(UserRepository.class);
 AuditLogRepository audit=mock(AuditLogRepository.class);
 RefreshTokenService tokens=mock(RefreshTokenService.class);
 BCryptPasswordEncoder passwords=new BCryptPasswordEncoder(4);
 AdminAccountsController controller=new AdminAccountsController(users,passwords,tokens,audit);
 User admin=User.builder().id(1).role(Role.ADMIN).email("admin@example.invalid").build();
 @Test void accountDetailsExposeOnlyManagementState(){
  User user=User.builder().id(2).role(Role.MERCHANT).userStatus(UserStatus.ACTIVE).enabled(true).passwordHash("secret-hash").build();when(users.findById(2L)).thenReturn(Optional.of(user));
  var result=controller.account(2L,admin);assertEquals(java.util.Set.of("id","status","role","enabled","locked"),result.keySet());assertFalse(result.toString().contains("secret-hash"));
 }
 @Test void suspensionRevokesSessionsAndAuditsOnce(){
  User user=User.builder().id(2).role(Role.USER).userStatus(UserStatus.ACTIVE).build();when(users.findForKycUpdate(2L)).thenReturn(Optional.of(user));
  var request=new AdminAccountsController.StatusRequest("SUSPENDED","Risk investigation");
  controller.status(2L,request,admin);controller.status(2L,request,admin);
  assertEquals(UserStatus.SUSPENDED,user.getUserStatus());verify(tokens,times(1)).revokeAllUserRefreshTokens(user);verify(audit,times(1)).save(any());
 }
 @Test void restoreCannotBypassMerchantReview(){
  User user=User.builder().id(2).role(Role.MERCHANT).userStatus(UserStatus.PENDING_REVIEW).build();when(users.findForKycUpdate(2L)).thenReturn(Optional.of(user));
  assertThrows(ResponseStatusException.class,()->controller.status(2L,new AdminAccountsController.StatusRequest("ACTIVE","Restore"),admin));verifyNoInteractions(tokens,audit);
 }
 @Test void adminAccountsAreProtected(){
  when(users.findForKycUpdate(1L)).thenReturn(Optional.of(admin));assertThrows(ResponseStatusException.class,()->controller.status(1L,new AdminAccountsController.StatusRequest("SUSPENDED","Test"),admin));verifyNoInteractions(audit,tokens);
 }
 @Test void provisioningHashesPasswordAndFixesRole(){
  when(users.saveAndFlush(any())).thenAnswer(invocation->{User user=invocation.getArgument(0);user.setId(3);return user;});
  var request=new AdminAccountsController.AgentRequest("Demo","Agent","+2207000000","AGENT@example.invalid","initial-password-123","New branch");
  assertEquals(3L,controller.createAgent(request,admin).get("id"));
  verify(users).saveAndFlush(argThat(user->user.getRole()==Role.AGENT && user.isEnabled() && !user.isPhoneVerified() && passwords.matches(request.password(),user.getPasswordHash()) && user.getEmail().equals("agent@example.invalid")));
  verify(audit).save(argThat(log->!log.getChangeDescription().contains(request.password())));
 }
 @Test void duplicateAgentAndNonAdminAreRejected(){
  var request=new AdminAccountsController.AgentRequest("Demo","Agent","+2207000000","agent@example.invalid","initial-password-123","New branch");
  when(users.existsByPhoneNumber(request.phoneNumber())).thenReturn(true);
  assertThrows(ResponseStatusException.class,()->controller.createAgent(request,admin));
  assertThrows(ResponseStatusException.class,()->controller.createAgent(request,User.builder().role(Role.AGENT).build()));verify(users,never()).saveAndFlush(any());
 }
}
