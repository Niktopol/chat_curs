"use client"

import Image from "next/image";
import styles from "./modules/chat_header.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { useEffect, useState } from "react";

export default function ChatHeader({ id, username, name, isPrivate }){
    const [editOpen, setEditOpen] = useState(false);
    const [chatImage, setChatImage] = useState("/default_user.svg");
    const [chatInfo, setChatInfo] = useState(" ");

    useEffect(() => {
        return () => {
            if (chatImage.startsWith('blob:')) {
                if (chatImage) URL.revokeObjectURL(chatImage);
            }
        }
    }, [chatImage]);

    useEffect(() => {
        const chatPicFetch = async () => {
            const chatpic_resp = await fetch(`http://localhost:8080/chats/image/${encodeURIComponent(id)}`, {credentials: "include"});

            if (chatpic_resp.ok) {
                const blob = await chatpic_resp.blob();
                setChatImage(URL.createObjectURL(blob));
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
        setChatImage("/default_user.svg");
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
        
    }, [id, username, name, isPrivate]);

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
                        <p className={styles.name}>{name}</p>
                        <p className={styles.status}>{chatInfo}</p>
                    </div>
                </div>
                <div className={styles.background}></div>
            </div>
            { editOpen ? 
                <div className={styles.chat_edit}>
                    <FontAwesomeIcon icon={faXmark} onClick={() => setEditOpen(false)} className={styles.close} />
                    { isPrivate ? 
                        <div className={styles.scroll_wrapper}>
                            <div className={styles.user_info}>
                                <div className={styles.user_image}>
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
                        </div>:
                        <></>
                    }
                </div> :
                <></>
            }
        </>
    );
}

/* <div className={styles.chat_info_wrapper}>
                        <form>
                            
                        </form>
                    </div>
                    <div className={styles.chat_members}>
                        
                    </div> */