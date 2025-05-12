import styles from "./modules/chat_panels.module.css";
import { ChatPanel } from "./chat";
import { useEffect, useRef, useState } from "react";
import { useSelector } from "react-redux";
import { useRouter } from "next/navigation";

export default function ChatPanels({ searchVal }){
    const session = useSelector((state) => state.userSession);
    const [chats, setChats] = useState({your: [], found: []});
    const [userChats, setUserChats] = useState(null);
    const [userFilterOption, setUserFilterOption] = useState(false);
    const [chatFilterOption, setChatFilterOption] = useState(false);
    const [page, setPage] = useState(0);
    const [doPagesLeft, setDoPagesLeft] = useState(false);
    const containerRef = useRef();
    const router = useRouter();

    const fetchChats = async () => {
            if (session.username) {
            try {
                const res = await fetch("http://localhost:8080/chats", {credentials: "include"});

                if (!res.ok) {
                    throw new Error((await res.json())?.error);
                }

                setUserChats(await res.json());
            } catch (e) {
                router.push("/login");
            }
        }
    }

    const findChatsByName = async () => {
        if (userChats) {
            if ((searchVal.length > 1 && searchVal[0] === "@") || (searchVal.length > 0 && searchVal[0] !== "@")){
                if (userFilterOption || (!userFilterOption && !chatFilterOption)) {
                    try {
                        const res = await fetch(`http://localhost:8080/chats/${encodeURIComponent(searchVal)}`, {credentials: "include"});
                        const searchChats = await res.json();
                        setPage(0);
                        setDoPagesLeft(!searchChats.last);
                        if (searchVal.length > 1 && searchVal[0] === "@") {
                            setChats({your: userChats.chats.filter(item =>
                                item.name.toLowerCase().startsWith(searchVal.toLowerCase())
                            ), found: searchChats.chats});
                        } else {
                            if (!userFilterOption && !chatFilterOption){
                                setChats({your: userChats.mixed.filter(item =>
                                    item.name.toLowerCase().includes(searchVal.toLowerCase())
                                ), found: searchChats.chats});
                            } else if (userFilterOption) {
                                setChats({your: userChats.chats.filter(item =>
                                    item.name.toLowerCase().includes(searchVal.toLowerCase())
                                ), found: searchChats.chats});
                            }
                        }
                    } catch (e) {
                        router.push("/login");
                    }
                } else {
                    setChats({your: userChats.groups.filter(item =>
                        item.name.toLowerCase().includes(searchVal.toLowerCase())
                    ), found: []});
                }
            } else {
                if (!userFilterOption && !chatFilterOption){
                    setChats({your: userChats.mixed, found: []});
                } else if (userFilterOption) {
                    setChats({your: userChats.chats, found: []});
                } else {
                    setChats({your: userChats.groups, found: []});
                }
            }
        }
    }

    useEffect(() => {
        fetchChats();
    }, [session]);

    useEffect(() => {
        findChatsByName();
    }, [searchVal, userChats, userFilterOption, chatFilterOption]);

    useEffect(() => {
        const handleScroll = async () => {
            const el = containerRef.current;
            if (doPagesLeft && (el.scrollTop + el.clientHeight >= el.scrollHeight)) {
                try {
                    const res = await fetch(`http://localhost:8080/chats/${encodeURIComponent(searchVal)}?page=${page + 1}`, {credentials: "include"});
                    const searchChats = await res.json();
                    setPage(page + 1);
                    setDoPagesLeft(!searchChats.last);
                    setChats({your: chats.your, found: [...chats.found, ...searchChats.chats]})
                } catch (e) {
                    router.push("/login");
                }
            }
        };

        const el = containerRef.current;
        el.addEventListener("scroll", handleScroll);
        return () => el.removeEventListener("scroll", handleScroll);
    }, [containerRef, searchVal, doPagesLeft, page, chats]);

    return (
        <div>
            <div className={styles.filter_options}>
                <div onClick={() => {
                    if (chatFilterOption) {
                        setChatFilterOption(false);
                    }
                    setUserFilterOption(!userFilterOption);
                    }} className={`${styles.option} ${userFilterOption ? styles.option_selected : styles.option_not_selected}`}>
                    <p className={styles.option_text}>Чаты</p>
                </div>
                <div onClick={() => {
                    if (userFilterOption) {
                        setUserFilterOption(false);
                    }
                    setChatFilterOption(!chatFilterOption);
                    }} className={`${styles.option} ${chatFilterOption ? styles.option_selected : styles.option_not_selected}`}>
                    <p className={styles.option_text}>Группы</p>
                </div>
            </div>
            <div className={styles.hide_wrapper}>
                <div ref={containerRef} className={styles.scroll_wrapper}>
                    { chats.your.length > 0 ?
                    (<>
                        <p className={styles.find_status}>Ваши чаты:</p>
                        {(chats.your.map((chat) => (<ChatPanel key={chat.id} data={chat}></ChatPanel>)))}
                    </>) :
                    (<></>) }
                    { chats.found.length > 0 ?
                    (<>
                        <p className={styles.find_status}>Чатов найдено:</p>
                        {(chats.found.map((chat) => (<ChatPanel key={chat.id} data={chat}></ChatPanel>)))}
                    </>) :
                    (<></>) }
                    { chats.your.length == 0 && chats.found.length == 0 ? <p className={styles.find_status}>Чатов не найдено</p> :<></>}
                </div>
            </div>
        </div>
    );
}