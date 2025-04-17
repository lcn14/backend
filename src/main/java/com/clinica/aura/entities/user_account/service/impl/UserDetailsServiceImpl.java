package com.clinica.aura.entities.user_account.service.impl;

import com.clinica.aura.config.jwt.JwtUtils;
import com.clinica.aura.entities.person.model.PersonModel;
import com.clinica.aura.entities.professional.dtoRequest.ProfessionalRequestDto;
import com.clinica.aura.entities.professional.model.ProfessionalModel;
import com.clinica.aura.entities.professional.repository.ProfessionalRepository;
import com.clinica.aura.entities.user_account.Enum.EnumRole;
import com.clinica.aura.entities.user_account.dtoRequest.AuthLoginRequestDto;
import com.clinica.aura.entities.user_account.dtoResponse.AuthResponseDto;
import com.clinica.aura.entities.user_account.dtoResponse.AuthResponseRegisterDto;
import com.clinica.aura.entities.user_account.models.RoleModel;
import com.clinica.aura.entities.user_account.models.UserModel;
import com.clinica.aura.entities.user_account.repository.RoleRepository;
import com.clinica.aura.entities.user_account.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Validated
public class UserDetailsServiceImpl implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final ProfessionalRepository professionalRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel userEntity = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
                "El usuario con el email " + email + "no existe"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        userEntity.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_".concat(role.getEnumRole().name())));
        });

        userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));

        return new User(userEntity.getEmail(),
                userEntity.getPassword(),
                true,
                true,
                true,
                true,
                authorities);
    }

    public AuthResponseDto loginUser(@Valid AuthLoginRequestDto authDto) {
        String email = authDto.getEmail();
        String password = authDto.getPassword();

        Long id = userRepository.findByEmail(email)
                .map(UserModel::getId)
                .orElseThrow(() -> new UsernameNotFoundException("El Id del usuario con el correo " + email + " no existe"));


        Authentication authentication = this.authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);


        String token = jwtUtils.generateJwtToken(authentication);
        return new AuthResponseDto(id, email, "Usuario logeado exitosamente", token, true);


    }

    public Authentication authenticate(String username, String password) {
        UserDetails userDetails = loadUserByUsername(username);
        if (userDetails == null) {
            throw new BadCredentialsException("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

    }

    public AuthResponseRegisterDto createUser(@Valid ProfessionalRequestDto authCreateUserDto) {

        String email = authCreateUserDto.getEmail();
        String password = authCreateUserDto.getPassword();
        String username = authCreateUserDto.getName();
        String lastName = authCreateUserDto.getLastName();
        String dni = authCreateUserDto.getDni();
        String phoneNumber = authCreateUserDto.getPhoneNumber();
        String country = authCreateUserDto.getCountry();
        String photoUrl = authCreateUserDto.getPhotoUrl();
        LocalDate birthDate = authCreateUserDto.getBirthDate();

        Optional<RoleModel> professionalRole = roleRepository.findByEnumRole(EnumRole.PROFESSIONAL);
        if (professionalRole.isEmpty()) {
            throw new IllegalArgumentException("El rol PROFESSIONAL no está configurado en la base de datos.");
        }

        Set<RoleModel> roleEntities = Set.of(professionalRole.get());


        PersonModel personEntity = PersonModel.builder()
                .dni(dni)
                .name(username)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .country(country)
                .birthDate(birthDate)
                .photoUrl(photoUrl)
                .build();

        ProfessionalModel professionalEntity = ProfessionalModel.builder()
                .person(personEntity)
                .licenseNumber(authCreateUserDto.getLicenseNumber())
                .specialty(authCreateUserDto.getSpecialty())
                .build();

        professionalRepository.save(professionalEntity);

        UserModel userEntity = UserModel.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(roleEntities)
                .person(personEntity)
                .build();

        UserModel userCreated = userRepository.save(userEntity);

        List<SimpleGrantedAuthority> authoritiesList = new ArrayList<>();

        userCreated.getRoles().forEach(role ->
                authoritiesList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getEnumRole().name())))
        );

        userCreated.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .forEach(permission -> authoritiesList.add(new SimpleGrantedAuthority(permission.getName())));

        Authentication authentication = new UsernamePasswordAuthenticationToken(userCreated.getEmail(), userCreated.getPassword(), authoritiesList);
        String accessToken = jwtUtils.generateJwtToken(authentication);

        return new AuthResponseRegisterDto(
                userCreated.getId(),
                username,
                "Usuario registrado exitosamente",
                accessToken,
                true
        );
    }
}
