package net.mapomi.mapomi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.mapomi.mapomi.common.PropertyUtil;
import net.mapomi.mapomi.common.error.UserNotFoundException;
import net.mapomi.mapomi.domain.user.Abled;
import net.mapomi.mapomi.domain.user.Disabled;
import net.mapomi.mapomi.domain.user.Observer;
import net.mapomi.mapomi.domain.user.User;
import net.mapomi.mapomi.dto.request.JoinDto;
import net.mapomi.mapomi.dto.request.LoginDto;
import net.mapomi.mapomi.jwt.*;
import net.mapomi.mapomi.repository.UserRepository;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtProvider;

    @Transactional
    public TokenDto login(LoginDto dto) {
        User user = userRepository.findByAccountId(dto.getId()).orElseThrow(UserNotFoundException::new);

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }
        TokenDto tokenDto = jwtProvider.createToken(user.getAccountId(), user.getId(), user.getRole());
        //리프레시 토큰 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(user.getId())
                .token(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenDto;
    }

    @Transactional
    public JSONObject signup(String type, JoinDto joinInfo) throws NullPointerException { //아이디 비번 이름 생일 통신사 번호 저장
        if(type.equals("disabled")){
            Disabled disabled = new Disabled(joinInfo);
            userRepository.save(disabled);
        }
        else if(type.equals("abled")){
            Abled abled = new Abled(joinInfo);
            userRepository.save(abled);
        }
        else {
            Observer observer = new Observer(joinInfo);
            userRepository.save(observer);
        }


        return PropertyUtil.response(true);
    }

//    @Transactional
//    public JSONObject changePW(Long id, String pw) {
//        User user = userRepository.findById(id).orElseThrow();
//        return PropertyUtil.response(user.changePW(passwordEncoder.encode(pw)));
//    }
//
//
//    @Transactional(readOnly = true)
//    public JSONObject checkPW(Long id, String pw) {
//        User user = userRepository.findById(id).orElseThrow();
//        if (!passwordEncoder.matches(pw, user.getPassword())) {
//            return PropertyUtil.response("비밀번호가 일치하지 않습니다.");
//        }
//        return PropertyUtil.response(true);
//    }
//
//    @Transactional(readOnly = true)
//    public JSONObject checkPW(String accountId, String pw) {
//        User user = userRepository.findByAccountId(accountId).orElseThrow();
//        if (!passwordEncoder.matches(pw, user.getPassword())) {
//            return PropertyUtil.response("비밀번호가 일치하지 않습니다.");
//        }
//        return PropertyUtil.response(true);
//    }

    @Transactional
    public TokenDto reissue(User User, TokenRequestDto tokenRequestDto) {
        // 만료된 refresh token 에러
        if (!jwtProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new NoSuchElementException();
        }

        List<RefreshToken> refreshTokens = refreshTokenRepository.findByKey(User.getId());
        RefreshToken refreshToken = refreshTokens.get(refreshTokens.size()-1); //마지막꺼가 가장 최신반영된 토큰
        // 리프레시 토큰 불일치 에러
        if (!refreshToken.getToken().equals(tokenRequestDto.getRefreshToken()))
            throw new NoSuchElementException();

        // AccessToken, RefreshToken 토큰 재발급, 리프레쉬 토큰 저장
        TokenDto newCreatedToken = jwtProvider.createToken(User.getAccountId(), User.getId(), User.getRole());
        RefreshToken updateRefreshToken = refreshToken.updateToken(newCreatedToken.getRefreshToken());
        refreshTokenRepository.save(updateRefreshToken);

        return newCreatedToken;
    }
}