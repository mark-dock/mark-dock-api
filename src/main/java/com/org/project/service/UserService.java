package com.org.project.service;

import com.org.project.exception.AccountExistsException;
import com.org.project.model.Folder;
import com.org.project.model.OrganizationUserRelation;
import com.org.project.model.User;
import com.org.project.dto.RegisterRequestDTO;
import com.org.project.repository.FolderRepository;
import com.org.project.repository.OrganizationUserRelationRepository;
import com.org.project.repository.UserRepository;

import com.org.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private OrganizationUserRelationRepository organizationUserRelationRepository;

    @Transactional
    public User registerUser(RegisterRequestDTO userRequest) {
        Optional<User> existingUser = userRepository.findByEmailAndProvider(userRequest.getEmail(), userRequest.getProvider());

        if (existingUser.isPresent()) {
            throw new AccountExistsException();
        }

        User newUser = new User();
        newUser.setName(userRequest.getName());
        newUser.setEmail(userRequest.getEmail());
        newUser.setProvider(userRequest.getProvider());

        if (newUser.getProvider() == User.Provider.LOCAL){
            newUser.setPasswordHash(userRequest.getPassword());
        }

        Folder rootFolder = new Folder();
        rootFolder.setName("root");
        rootFolder.setUser(newUser);

        try {
            User savedUser = userRepository.save(newUser);
            folderRepository.save(rootFolder);
            return savedUser;
        } catch (Exception e) {
            throw e;
        }
    }

    public User getUserFromId(String userId) {
        return userRepository.findById(userId);
    }

    public void updateAuthVersion(String userId) {
        User user = userRepository.findById(userId);
        user.setAuthVersion(AuthUtil.generateRandomAuthVersion());
        userRepository.save(user);
    }

    public OrganizationUserRelation isUserOrganizationMember(String userId, String organizationId){
        return organizationUserRelationRepository.findByUserIdAndOrganizationId(userId, organizationId);
    }

    public Integer getAccessId(String userId, String organizationId) {
        OrganizationUserRelation relation = organizationUserRelationRepository.findByUserIdAndOrganizationId(userId, organizationId);
        return (relation != null) ? relation.getAccessId() : null;
    }
}
