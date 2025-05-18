"use client"

import { useEffect, useRef, useState } from "react";
import styles from "./modules/messages.module.css"
import { useDispatch, useSelector } from "react-redux";
import { addMessages, postMessage, setMessages } from "@/store/chatSelectedSlice";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { API_URL } from "@/lib/config";

function Message({ data, profiles }){
    const session = useSelector((state) => state.userSession);
    const [msgImage, setMsgImage] = useState(null);
    const router = useRouter();

    useEffect(() => {
        return () => {
            if (msgImage && msgImage.startsWith('blob:')) {
                URL.revokeObjectURL(msgImage);
            }
        }
    }, [msgImage]);

    useEffect(() => {
        const fetchMsgImage = async () => {
            try {
                const resp = await fetch(`${API_URL}/messages/image/${data.id}`, {credentials: "include"});
            
                if (resp.ok) {
                    const blob = await resp.blob();
                    setMsgImage(URL.createObjectURL(blob));
                } else {
                    throw new Error((await user_resp.json())?.error);
                }
            } catch (e) {
                router.push("/login");
            }
        }

        if (data.attachment) {
            fetchMsgImage();
        }
    }, [data])

    return (
        !data.attachment || msgImage ?
            <div className={`${styles.message_base} ${data.sender === session.username ? styles.message_my : styles.message_others}`}>
                <div className={styles.sender_profile}>
                    <p className={styles.sender_username}><span>@</span>{data.sender}</p>
                    <div className={styles.sender_pic}>
                        <Image src={profiles?.[data.sender] ? profiles[data.sender] : "/chat_curs/default_user.svg"} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
                    </div>
                </div>
                <div className={styles.cloud}>
                    <div className={styles.triangle}></div>
                    { !data.attachment ?
                    <p className={styles.msg_text}>{data.text}</p> :
                    <div className={styles.msg_text}><Image src={msgImage} alt="" width={0} height={0} sizes="(max-width: 100%)" className={styles.msg_image}></Image></div>}
                </div>
            </div> :
            <></>
        );
        
}

export default function Messages(){
    const chat = useSelector((state) => state.chatSelected);
    const websocket = useSelector((state) => state.websocketMessage);
    const containerRef = useRef(null);
    const topRef = useRef(null);
    const [msgPage, setMsgPage] = useState(0);
    const [isPageFinal, setIsPageFinal] = useState(false);
    const [profiles, setProfiles] = useState(null);
    const dispatch = useDispatch();
    const router = useRouter();
    const messagesRef = useRef(chat.messages);
    const pageRef = useRef(msgPage);
    const pageFinalRef = useRef(isPageFinal);

    const fetchMessages = async (page) => {
        try {
            const messages = await fetch(`${API_URL}/messages/${chat.id}?page=${page}`, {credentials: "include"});

            if (!messages.ok) {
                throw new Error((await user_resp.json())?.error);
            } else {
                const resp = await messages.json();
                setMsgPage(resp.page);
                setIsPageFinal(resp.last);
                if (page == 0) {
                    dispatch(setMessages(resp.messages));
                } else {
                    dispatch(addMessages(resp.messages));
                }
            }
        } catch (e) {
            router.push("/login");
        }
    }

    const fetchUserPic = async (username) => {
        try {
            const userpic_resp = await fetch(`${API_URL}/user/profilepic/${encodeURIComponent(username)}`, {credentials: "include"});
        
            if (userpic_resp.ok) {
                const blob = await userpic_resp.blob();
                return URL.createObjectURL(blob);
            } else {
                return "/chat_curs/default_user.svg";
            }
        } catch (e) {
            router.push("/login");
        }
    }

    const loadUserPics = async () => {
        const entries = await Promise.all(
            chat.users.map(async (user) => {
                const pic = await fetchUserPic(user.username);
                return [user.username, pic];
            })
        );

        const profilesObj = Object.fromEntries(entries);
        setProfiles(profilesObj);
    };

    useEffect(() => {
        messagesRef.current = chat.messages;
    }, [chat.messages]);

    useEffect(() => {
        pageRef.current = msgPage;
    }, [msgPage]);

    useEffect(() => {
        pageFinalRef.current = isPageFinal;
    }, [isPageFinal]);

    useEffect(() => {
        if (!containerRef.current || !topRef.current) return;

        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting && messagesRef.current.length > 0 && !pageFinalRef.current){
                    fetchMessages(pageRef.current + 1);
                }
            },
            {
                root: containerRef.current,
                threshold: 0.1,
            }
        );

        observer.observe(topRef.current);

        return () => {
            observer.disconnect();
        };
    }, []);

    useEffect(() => {
        return () => {
            if (profiles) {
                Object.values(profiles).forEach(userPic => {
                    if (userPic.startsWith('blob:')) {
                        URL.revokeObjectURL(userPic);
                    }
                });
            }
        }
    }, [profiles])

    useEffect(() => {
        dispatch(setMessages([]));
        setMsgPage(0);
        setIsPageFinal(false);
        if (!chat.new) {
            fetchMessages(0);
        }
    }, [chat.id, chat.username]);

    useEffect(() => {
        if (!chat.new && chat.users && chat.users.length > 0) {
            loadUserPics();
        }
    }, [chat.users])

    useEffect(() => {
        if (!chat.new && websocket.message?.title === "Message received" && websocket.message.id == chat.id) {
            dispatch(postMessage(websocket.message.message));
        }

        if (!chat.new && websocket.message?.title === "User info updated" && profiles.hasOwnProperty(websocket.message.username)) {
            loadUserPics();
        }
    }, [websocket])

    return (
        <div className={styles.main}>
            <div ref={containerRef} className={styles.scroll_wrapper}>
                {chat.new ? 
                    <></> :
                    chat.messages.map( (message) => (<Message key={message.id} data={message} profiles={profiles}></Message>)) 
                }
                <div ref={topRef}></div>
            </div>
        </div>
    );
}