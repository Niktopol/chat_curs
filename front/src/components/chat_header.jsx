"use client"

import Image from "next/image";
import styles from "./modules/chat_header.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileImport, faXmark } from "@fortawesome/free-solid-svg-icons";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";

export default function ChatHeader({ id, username, name, isPrivate }){
    const [editOpen, setEditOpen] = useState(false);
    const [chatImage, setChatImage] = useState("/default_user.svg");
    const [newChatImage, setNewChatImage] = useState("/default_user.svg");
    const [chatInfo, setChatInfo] = useState(" ");
    const [isEditing, setIsEditing] = useState(false);
    const [doDelChatPic, setDoDelChatPic] = useState(false);
    const [imageErrorMsg, setImageErrorMsg] = useState("");
    const [currentName, setCurrentName] = useState(name);
    const {
        register,
        handleSubmit,
        setValue,
        reset,
        formState: { errors, isSubmitting },
    } = useForm({defaultValues: {name: name}});

    const resetEditing = (newImg, newName=currentName) => {
        setIsEditing(false);
        setImageErrorMsg("");
        setChatImage(newImg);
        setNewChatImage(newImg);
        setDoDelChatPic(false);
        setCurrentName(newName);
        reset({name: newName});
    }

    useEffect(() => {
        return () => {
            if (chatImage.startsWith('blob:')) {
                if (chatImage) URL.revokeObjectURL(chatImage);
            }
        }
    }, [chatImage]);

    useEffect(() => {
        if (doDelChatPic) setNewChatImage("/default_user.svg");
    }, [doDelChatPic]);

    useEffect(() => {
        const chatPicFetch = async () => {
            const chatpic_resp = await fetch(`http://localhost:8080/chats/image/${id}`, {credentials: "include"});

            if (chatpic_resp.ok) {
                const blob = await chatpic_resp.blob();
                const url = URL.createObjectURL(blob)
                setChatImage(url);
                setNewChatImage(url);
            }
        }

        const userPicFetch = async () => {
            const userpic_resp = await fetch(`http://localhost:8080/user/profilepic/${encodeURIComponent(username)}`, {credentials: "include"});

            if (userpic_resp.ok) {
                const blob = await userpic_resp.blob();
                setChatImage(URL.createObjectURL(blob));
            }
        }

        const userStatusFetch = async () => {
            const user_resp = await fetch(`http://localhost:8080/user/profile/${encodeURIComponent(username)}`, {credentials: "include"});

            if (user_resp.ok) {
                setChatInfo((await user_resp.json()).isOnline ? "В сети" : "Не в сети")
            } else {
                throw new Error((await res.json())?.error);
            }
        }
    
        setEditOpen(false);
        setChatImage();
        resetEditing("/default_user.svg", name);
        setChatInfo(" ");
        
        try {
            if (isPrivate) {
                userPicFetch();
                userStatusFetch();
            } else {
                chatPicFetch();
            }
        } catch (e) {
            router.push("/login");
        }
        
    }, [id, username]);

    const onSubmit = async (data) => {
        resetEditing(newChatImage, data.name);
    }

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
                        <p className={styles.name}>{currentName}</p>
                        <p className={styles.status}>{chatInfo}</p>
                    </div>
                </div>
                <div className={styles.background}></div>
            </div>
            { editOpen ? 
                <div className={styles.chat_edit}>
                    <FontAwesomeIcon icon={faXmark} onClick={() => setEditOpen(false)} className={styles.close} />
                    { isPrivate ? 
                        (<div className={styles.scroll_wrapper}>
                            <div className={styles.user_info}>
                                <div className={`${styles.chat_image} ${styles.disabled}`}>
                                    <Image src={chatImage} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
                                </div>
                                <div className={styles.user_data}>
                                    <p className={styles.user_detail}><span>@</span>{username}</p>
                                    <div className={styles.user_name_wrapper}>
                                        <p className={styles.user_name}>{name}</p>
                                    </div>
                                    <p className={styles.user_detail}>{chatInfo}</p>
                                </div>
                            </div>
                        </div>) :
                        (<div className={styles.scroll_wrapper}>
                            <form onSubmit={handleSubmit(onSubmit)} className={styles.user_info}>
                                <div className={styles.image_input_wrapper}>
                                    <label htmlFor="chatimage" className={`${styles.chat_image} ${!isEditing ? styles.disabled: ""}`}>
                                        <Image src={newChatImage} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
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
                                                setNewChatImage(URL.createObjectURL(e.target.files[0]));
                                                setDoDelChatPic(false);
                                                setImageErrorMsg("")
                                            } else {
                                                setNewChatImage(chatImage);
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
                                        placeholder={currentName}
                                        autoComplete="off"
                                        autoCorrect="off"
                                        className={styles.name_input}
                                        {...register("name", {
                                            required: "Введите название чата",
                                            maxLength: {
                                                value: 50,
                                                message: "Значение должно быть короче 50 символов"
                                            },
                                            pattern: {
                                                value: /\S/,
                                                message: "Некорректное название чата"
                                            },
                                        })}
                                        disabled={!isEditing || isSubmitting}/>
                                        <p className={styles.error_msg}>{errors.name?.message ? errors.name?.message : ""}</p>
                                    </div>
                                    <p className={styles.user_detail}>{chatInfo}</p>
                                    <div className={styles.buttons}>
                                        {!isEditing && (<input type="button" onClick={() => setIsEditing(true)} value={"Изменить"} className={styles.edit_button}/>)}
                                        {isEditing && (<>
                                            <input type="button" onClick={() => setDoDelChatPic(true)} value={"Удалить фото"} className={styles.edit_button}/>
                                            <input type="submit" value={"Сохранить"} disabled={isSubmitting} className={styles.edit_button}/>
                                            <input type="button" onClick={() => resetEditing(chatImage)} value={"Отменить"} disabled={isSubmitting} className={styles.edit_button}/>
                                        </>)}
                                    </div>
                                </div>
                            </form>
                        </div>)
                    }
                </div> :
                <></>
            }
        </>
    );
}