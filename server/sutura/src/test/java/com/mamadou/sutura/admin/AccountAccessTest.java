package com.mamadou.sutura.admin;
import com.mamadou.sutura.auth.service.JwtAuthenticationFilter;
import com.mamadou.sutura.common.config.JWTConfig;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.user.enums.*;
import com.mamadou.sutura.user.repository.UserRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AccountAccessTest {
 @Test void existingAccessTokenIsDeniedForRestrictedAccounts() throws Exception {
  JWTConfig jwt=mock(JWTConfig.class);UserRepository users=mock(UserRepository.class);Claims claims=mock(Claims.class);
  when(jwt.validateToken("token")).thenReturn(true);when(jwt.extractAllClaims("token")).thenReturn(claims);
  when(claims.get("type",String.class)).thenReturn("access_token");when(jwt.extractSubject("token")).thenReturn("+2207000000");
  var filter=new JwtAuthenticationFilter(jwt,users);
  try{for(UserStatus status:new UserStatus[]{UserStatus.SUSPENDED,UserStatus.BANNED,UserStatus.ACTIVE}){
   SecurityContextHolder.clearContext();User user=User.builder().id(2).phoneNumber("+2207000000").role(Role.AGENT).enabled(true).userStatus(status).build();
   when(users.findByLoginIdentifier("+2207000000")).thenReturn(Optional.of(user));
   var request=new MockHttpServletRequest();request.addHeader("Authorization","Bearer token");
   filter.doFilter(request,new MockHttpServletResponse(),(req,res)->{});
   assertEquals(status==UserStatus.ACTIVE,SecurityContextHolder.getContext().getAuthentication()!=null);
  }}finally{SecurityContextHolder.clearContext();}
 }
}
