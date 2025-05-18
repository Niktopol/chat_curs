"use client"

import Image from "next/image";
import styles from "./modules/chat_header.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileImport, faXmark } from "@fortawesome/free-solid-svg-icons";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { useDispatch, useSelector } from "react-redux";
import { setChat, setUsers } from "@/store/chatSelectedSlice";
import ChatUserPanel from "./chat_user_panel";
import ChatUserAddInput from "./chat_user_add_input";
import { API_URL } from "@/lib/config";

function ChatDataOverlay({ image, editOpen, setEditOpen }) {
    const session = useSelector((state) => state.userSession);
    const chat = useSelector((state) => state.chatSelected);
    const [isEditing, setIsEditing] = useState(false);
    const [chatImage, setChatImage] = useState(image);
    const [chatInfo, setChatInfo] = useState(" ");
    const [isChatOwner, setIsChatOwner] = useState(false);
    const [doDelChatPic, setDoDelChatPic] = useState(false);
    const [imageErrorMsg, setImageErrorMsg] = useState("");
    const router = useRouter();
    const {
        register,
        handleSubmit,
        setValue,
        reset,
        formState: { errors, isSubmitting },
    } = useForm({defaultValues: {name: chat.name}});

    const fetchUserStatus = async () => {
        try {
            const user_resp = await fetch(`${API_URL}/user/profile/${encodeURIComponent(chat.username)}`, {credentials: "include"});

            if (user_resp.ok) {
                setChatInfo((await user_resp.json()).isOnline ? "В сети" : "Не в сети")
            } else {
                throw new Error((await user_resp.json())?.error);
            }
        } catch (e) {
            router.push("/login");
        }
    }

    const resetEditing = () => {
        setIsEditing(false);
        setImageErrorMsg("");
        setDoDelChatPic(false);
        reset({name: chat.name});
    }

    useEffect(() => {
        if (!isEditing) {
            setChatImage(image);
        }
    }, [image, isEditing]);

    useEffect(() => {
        return () => {
            if (chatImage !== image && chatImage.startsWith('blob:')) {
                URL.revokeObjectURL(chatImage);
            }
        }
    }, [chatImage]);

    useEffect(() => {
        if (!isEditing) {
            reset({name: chat.name});
        }
    }, [chat.name])

    useEffect(() => {
        resetEditing();

    }, [chat.id, chat.username, chat.new]);

    useEffect(() => {
        if (!chat.private) {
            let isOwner = false;
            for (let i = 0; i < chat.users.length; i++){
                if (chat.users[i]?.isOwner && chat.users[i].username === session.username){
                    isOwner = true;
                    break;
                }
            }
            setIsChatOwner(isOwner);

            if (chat.users) {
                setChatInfo(`Участников: ${chat.users.length}`);
            }
        } else {
            fetchUserStatus();
        }
        
    }, [chat.id, chat.username, chat.users, chat.new]);
    
    useEffect(() => {
        if (doDelChatPic) {
            setValue("file", null);
            setChatImage("/chat_curs/default_user.svg");
        }
    }, [doDelChatPic]);

    const onSubmit = async (data) => {
        try {
            if (data.file && data.file.length) {
                const formData = new FormData();
                formData.append('file', data.file[0]);
                const res = await fetch(`${API_URL}/chats/image/${chat.id}`, {
                    method: "PATCH",
                    credentials: "include",
                    body: formData
                });

                if (!res.ok) {
                    setChatImage(image);
                    setValue("file", null);
                    setImageErrorMsg((await res.json())?.error);
                    return;
                }

            } else if (doDelChatPic) {
                const res = await fetch(`${API_URL}/chats/image/${chat.id}`, {
                    method: "DELETE",
                    credentials: "include"
                });

                if (!res.ok) {
                    throw new Error((await res.json())?.error);
                }
            }

            const res = await fetch(`${API_URL}/chats/${chat.id}`, {
                method: "PATCH",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ value: data.name }),
            });

            if (!res.ok) {
                throw new Error((await res.json())?.error);
            }
            resetEditing();
        } catch (e) {
            router.push("/login");
        }
    }

    const leaveChat = async () => {
        try {
            const leave_chat_resp = await fetch(`${API_URL}/chats/users/${chat.id}/delete`, {
                method: "PATCH",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({value: session.username}),
            });
            if (!leave_chat_resp.ok){
                router.push("/login");
            }
        } catch (e) {
            router.push("/login");
        }
    }

    return (
        <div className={styles.chat_edit} style={editOpen ? null : {display: "none"}}>
            <FontAwesomeIcon icon={faXmark} onClick={() => setEditOpen(false)} className={styles.close} />
            { chat.private ? 
                (<div className={styles.scroll_wrapper}>
                    <div className={styles.user_info}>
                        <div className={`${styles.chat_image} ${styles.disabled}`}>
                            <Image src={image} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
                        </div>
                        <div className={styles.user_data}>
                            <p className={styles.user_detail}><span>@</span>{chat.username}</p>
                            <div className={styles.user_name_wrapper}>
                                <p className={styles.user_name}>{chat.name}</p>
                            </div>
                            <p className={styles.user_detail}>{chatInfo}</p>
                        </div>
                    </div>
                </div>) :
                (<div className={styles.scroll_wrapper}>
                    <form onSubmit={handleSubmit(onSubmit)} className={styles.user_info}>
                        <div className={styles.image_input_wrapper}>
                            <label htmlFor="chatimage" className={`${styles.chat_image} ${!isEditing ? styles.disabled: ""}`}>
                                <Image src={chatImage} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
                                <div className={styles.image_edit_cover}>
                                    <FontAwesomeIcon icon={faFileImport} className={styles.image_edit_cover_icon} />
                                </div>
                            </label>
                            <input type="file"
                            accept="image/jpeg"
                            id="chatimage"
                            style={{display: "none"}}
                            {...register("file", {
                                onChange: (e) => {
                                    if (e.target.files[0].size < 5 * 1024 * 1024) {
                                        setChatImage(URL.createObjectURL(e.target.files[0]));
                                        setDoDelChatPic(false);
                                        setImageErrorMsg("")
                                    } else {
                                        setChatImage(chatImage);
                                        setValue("file", null);
                                        setImageErrorMsg("Размер изображения > 5 МБ")
                                    }
                                },
                                validate: {
                                    lessThan5MB: (fileList) => {
                                        if (fileList){
                                            if (fileList[0]?.size > 5 * 1024 * 1024) {
                                                setImageErrorMsg("Размер изображения > 5 МБ")
                                                return "Размер изображения больше 5 МБ";
                                            } else {
                                                setImageErrorMsg("")
                                                return true;
                                            }
                                        }
                                    }
                                },
                            })}
                            disabled={!isEditing || isSubmitting}/>
                            <p className={styles.file_error_msg}>{imageErrorMsg}</p>
                        </div>
                        <div className={styles.user_data}>
                            <div className={styles.user_name_wrapper}><input
                                placeholder={chat.name}
                                autoComplete="off"
                                autoCorrect="off"
                                className={styles.name_input}
                                {...register("name", {
                                    required: "Введите название чата",
                                    maxLength: {
                                        value: 30,
                                        message: "Значение должно быть короче 30 символов"
                                    },
                                    pattern: {
                                        value: /^.*\S.*$/,
                                        message: "Некорректное название чата"
                                    }
                                })}
                                disabled={!isEditing || isSubmitting}/>
                                <p className={styles.error_msg}>{errors.name?.message ? errors.name?.message : ""}</p>
                            </div>
                            <p className={styles.user_detail}>{chatInfo}</p>
                            {  isChatOwner ? 
                                <div className={styles.buttons}>
                                    {!isEditing && (<input type="button" onClick={() => setIsEditing(true)} value={"Изменить"} className={styles.edit_button}/>)}
                                    {isEditing && (<>
                                        <input type="button" onClick={() => setDoDelChatPic(true)} value={"Удалить фото"} disabled={isSubmitting} className={styles.edit_button}/>
                                        <input type="submit" value={"Сохранить"} disabled={isSubmitting} className={styles.edit_button}/>
                                        <input type="button" onClick={() => resetEditing(chatImage)} value={"Отменить"} disabled={isSubmitting} className={styles.edit_button}/>
                                    </>)}
                                </div> :
                                <></>
                            }
                        </div>
                    </form>
                    { !chat.private ? <button onClick={leaveChat} className={styles.leave_chat_btn}>Покинуть чат</button> : <></> }
                    { isChatOwner ? <ChatUserAddInput id={chat.id}></ChatUserAddInput> : <></> }
                    <h2>Участники</h2>
                    <div className={styles.chat_users}>
                        {chat.users.map((user) => (<ChatUserPanel key={user.id} chatId={chat.id} data={user} isOwner={isChatOwner}></ChatUserPanel>))}
                    </div>
                </div>)
            }
        </div>
    );
}

export default function ChatHeader(){
    const chat = useSelector((state) => state.chatSelected);
    const websocket = useSelector((state) => state.websocketMessage);
    const [editOpen, setEditOpen] = useState(false);
    const [chatImage, setChatImage] = useState("/chat_curs/default_user.svg");
    const [chatInfo, setChatInfo] = useState(" ");
    const dispatch = useDispatch();

    useEffect(() => {
        return () => {
            if (chatImage.startsWith('blob:')) {
                URL.revokeObjectURL(chatImage);
            }
        }
    }, [chatImage]);

    const fetchChatImage = async () => {
        if (chat.private){
            try {
                const userpic_resp = await fetch(`${API_URL}/user/profilepic/${encodeURIComponent(chat.username)}`, {credentials: "include"});

                if (userpic_resp.ok) {
                    const blob = await userpic_resp.blob();
                    setChatImage(URL.createObjectURL(blob));
                } else {
                    setChatImage("/chat_curs/default_user.svg");
                }
            } catch (e) {
                dispatch(setChat(
                {
                    id: null,
                    username: "",
                    name: "",
                    private: null,
                    new: null
                }));
            }
        } else {
            try {
                const chatpic_resp = await fetch(`${API_URL}/chats/image/${chat.id}`, {credentials: "include"})
                if (chatpic_resp.ok) {
                    const blob = await chatpic_resp.blob();
                    const url = URL.createObjectURL(blob)
                    setChatImage(url);
                } else {
                    setChatImage("/chat_curs/default_user.svg");
                }
            } catch (e) {
                dispatch(setChat(
                {
                    id: null,
                    username: "",
                    name: "",
                    private: null,
                    new: null
                }));
            }
        }
    }

    const fetchChatInfo = async () => {
        if (chat.private) {
            try {
                const user_resp = await fetch(`${API_URL}/user/profile/${encodeURIComponent(chat.username)}`, {credentials: "include"});

                if (user_resp.ok) {
                    setChatInfo((await user_resp.json()).isOnline ? "В сети" : "Не в сети")
                } else {
                    throw new Error((await user_resp.json())?.error);
                }

                if (!chat.new) {
                    const chat_users_resp = await fetch(`${API_URL}/chats/users/${chat.id}`, {credentials: "include"});
      
                    if (chat_users_resp.ok) {
                        const users = await chat_users_resp.json();
                        dispatch(setUsers(users));
                    } else {
                        throw new Error((await chat_users_resp.json())?.error);
                    }
                }
                
            } catch (e) {
                dispatch(setChat(
                {
                    id: null,
                    username: "",
                    name: "",
                    private: null,
                    new: null
                }));
            }
        } else {
            try {
                const resp = await fetch(`${API_URL}/chats/${chat.id}`, {credentials: "include"});

                if (!resp.ok) {
                    throw new Error((await resp.json())?.error);
                } else {
                    const data = await resp.json();
                    dispatch(setChat(data));
                }

                const chat_users_resp = await fetch(`${API_URL}/chats/users/${chat.id}`, {credentials: "include"});
      
                if (chat_users_resp.ok) {
                    const users = await chat_users_resp.json();
                    dispatch(setUsers(users));
                    setChatInfo(`Участников: ${users.length}`);
                } else {
                    throw new Error((await chat_users_resp.json())?.error);
                }
            } catch (e) {
                dispatch(setChat(
                {
                    id: null,
                    username: "",
                    name: "",
                    private: null,
                    new: null,
                }));
            }
        }
    }

    useEffect(() => {
        setChatImage("/chat_curs/default_user.svg");
        setChatInfo(" ");
        fetchChatInfo();
        fetchChatImage();
        setEditOpen(false);
    }, [chat.id, chat.username, chat.new])


    useEffect(() => {
        if (!chat.private && websocket.message?.title === "Chat info updated" && websocket.message.id == chat.id) {
            fetchChatInfo();
        }
        if (!chat.private && websocket.message?.title === "Chat image updated" && websocket.message.id == chat.id) {
            fetchChatImage();
        }
        if (!chat.new && chat.private && websocket.message?.title === "User went online" && websocket.message.username == chat.username) {
            setChatInfo("В сети");
        }
        if (!chat.new && chat.private && websocket.message?.title === "User went offline" && websocket.message.username == chat.username) {
            setChatInfo("Не в сети");
        }
    }, [websocket])

    return (
        <>
            <div onClick={() => setEditOpen(true)} className={styles.main}>
                <div className={styles.content}>
                    <div className={styles.image}>
                        <div className={styles.image_wrapper}>
                            <Image src={chatImage} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
                        </div>
                    </div>
                    <div className={styles.info}>
                        <p className={styles.name}>{chat.name}</p>
                        <p className={styles.status}>{chatInfo}</p>
                    </div>
                </div>
                <div className={styles.background}></div>
            </div>
            <ChatDataOverlay image={chatImage} editOpen={editOpen} setEditOpen={setEditOpen}></ChatDataOverlay>
        </>
    );
}