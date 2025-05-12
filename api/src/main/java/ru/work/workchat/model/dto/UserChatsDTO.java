package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.work.workchat.model.entity.Chat;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
@Setter
public class UserChatsDTO {
    List<ChatDTO> chats;
    List<ChatDTO> groups;
    List<ChatDTO> mixed;

    public UserChatsDTO(List<Chat> chats, List<Chat> groups, String username){
        this.chats = chats.stream().map(chat ->
            new ChatDTO(
                    chat.getId(),
                    chat.getChatUsers().get(0).getUser().getUsername().equals(username) ?
                            chat.getChatUsers().get(1).getUser().getUsername() :
                            chat.getChatUsers().get(0).getUser().getUsername(),
                    chat.getChatUsers().get(0).getUser().getUsername().equals(username) ?
                            chat.getChatUsers().get(1).getUser().getName() :
                            chat.getChatUsers().get(0).getUser().getName(),
                    chat.isPrivate(),
                    false
            )
        ).toList();

        this.groups = groups.stream().map(group ->
                new ChatDTO(
                        group.getId(),
                        null,
                        group.getName(),
                        group.isPrivate(),
                        false
                )
        ).toList();

        this.mixed = Stream.concat(chats.stream(), groups.stream())
                .sorted(Comparator.comparing(Chat::getLastMessageTime).reversed())
                .map(chat -> {
                    if (chat.isPrivate()){
                        return new ChatDTO(
                                chat.getId(),
                                chat.getChatUsers().get(0).getUser().getUsername().equals(username) ?
                                        chat.getChatUsers().get(1).getUser().getUsername() :
                                        chat.getChatUsers().get(0).getUser().getUsername(),
                                chat.getChatUsers().get(0).getUser().getUsername().equals(username) ?
                                        chat.getChatUsers().get(1).getUser().getName() :
                                        chat.getChatUsers().get(0).getUser().getName(),
                                chat.isPrivate(),
                                false
                        );
                    } else {
                        return new ChatDTO(
                                chat.getId(),
                                null,
                                chat.getName(),
                                chat.isPrivate(),
                                false
                        );
                    }
                }).toList();
    }
}
