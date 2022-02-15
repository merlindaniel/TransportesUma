let myJourneyApp = new Vue({
    el: '#chats',
    data: {
        tokenConBearer: null,
        loggedUser: {},
        chats: [],
        selectedChat: null,
        updated: false,
        currentMessage: '',
        usersInfo : [],
        temporaryNewUser: null,

        newChat: false,
        chatSelected: false,
        noChats: false,

        newMessageRecipient: '',
        possibleRecipient:  null,
        searchError: null,
    },
    mounted: function () {
        if(this.chats.length == 0){
            this.setNoChats();
        }
        window.setInterval(() => {
          this.obtenerChats()
        }, 1000)
    },
    methods: {
        async getLoggedUser() {
            let response = await fetch('/api/users/current', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization':this.tokenConBearer
                }});
            this.loggedUser = await response.json();
            console.log("Logged user: " + this.loggedUser);

        },

        async obtenerChats() {
            console.log('LOG: chatSelected: ' + this.chatSelected + ' newChat: '+ this.newChat + ' noChats: ' + this.noChats);

            updated = false;
            let response = await axios.get('/api/chats/current', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.tokenConBearer
                }
            });
            if(JSON.stringify(response.data) !== JSON.stringify(this.chats)){
                updated = true;
                console.log('Chats were updated:');
                this.chats = JSON.parse(JSON.stringify(response.data));
                console.log(this.chats);
            }
            
            if(!this.newChat && updated){
                if(this.chatSelected == false){
                    this.selectChat(this.chats.at(0));
                }else{
                    this.selectChat(this.chats.find(x => x.id == this.selectedChat.id));
                }
            }
            
            if(updated){
                await this.getUsers();
            }
        },

        setNoChats() {
            this.noChats = true;
            this.newChat = false;
            this.chatSelected = false;
            console.log('NO CHATS: chatSelected: ' + this.chatSelected + ' newChat: '+ this.newChat + ' noChats: ' + this.noChats);
        },

        async getUsers() {
            for(i = 0; i < this.chats.length; i++){
                user = await this.getUserAPI(JSON.parse(JSON.stringify(this.chats[i])).user1);
                if(this.usersInfo.find(x => x.id == user.id) === undefined){
                    this.usersInfo.push(user);
                }
                user = await this.getUserAPI(JSON.parse(JSON.stringify(this.chats[i])).user2);
                if(this.usersInfo.find(x => x.id == user.id) === undefined){
                    this.usersInfo.push(user);
                }
            }
            console.log('Users info: ');
            console.log(this.usersInfo);
        },

        async getUserAPI(idUser){
            let response = await fetch('/api/users/searchUser/'+idUser, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.tokenConBearer
                }});
                if(response.ok) {
                    user = await response.json();
                    return user;
                }else{
                    return null;
                }
        },

        selectChat: function(chat) {
            console.log('CHAT SELECTED: ');
            console.log(chat);
            this.clearNewChat();
            this.noChats = false;
            this.selectedChat = chat;
            this.chatSelected = true;
            console.log('SELECT CHAT: chatSelected: ' + this.chatSelected + ' newChat: '+ this.newChat + ' noChats: ' + this.noChats);
        },

        clearNewChat() {
            this.newChat = false;
            this.newMessageRecipient = '';
            this.searchError = null;
            this.possibleRecipient = null;
        },

        crearNuevoMensaje: function(event) {
            this.chatSelected = false;
            this.newChat = true;
            this.selectedChat = null;
            console.log('NEW CHAT: chatSelected: ' + this.chatSelected + ' newChat: '+ this.newChat + ' noChats: ' + this.noChats);
        },

        startNewChat: function(userId) {
            this.temporaryNewUser = null;   
            foundchat = null;
            for(i = 0; i < this.chats.length; i++){
                if(this.chats[i].user1 == this.loggedUser.id && this.chats[i].user2 == userId ||
                    this.chats[i].user2 == this.loggedUser && this.chats[i].user1 == userId){
                        foundchat = this.chats[i];
                        console.log('Existing chat found');
                        this.selectChat(foundchat);
                    }
            }
            if(foundchat == null){
                console.log('Creating new chat');
                newChat = {user1: this.loggedUser.id, user2: userId, messages: []};
                console.log(newChat);
                this.changeTemporaryNewUser(userId);
                this.selectChat(newChat);
            }
        },

        async changeTemporaryNewUser(newUserId){
            let response = await fetch('/api/users/searchUser/'+newUserId, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': this.tokenConBearer
                }});
                if(response.ok) {
                    user = await response.json();
                    this.temporaryNewUser = user;
                }
        },

        sendMessage: function() {
            this.enviarMensaje();
        },

        async enviarMensaje() {
            if(this.currentMessage.length > 0){
                newMessage = {user:this.loggedUser.id, message: this.currentMessage, date: new Date()};
                this.selectedChat.messages.push(newMessage);
                if(this.selectedChat.messages.length == 1){
                    let response = await axios.post('/api/chats/current/newChat', this.selectedChat, {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization':this.tokenConBearer
                        }
                    });
                    if(response){
                        this.selectChat(response.data);
                        this.obtenerChats();
                        this.temporaryNewUser = null;
                    }else{
                        console.log('Error');
                    }
                }else{
                    let response = await axios.put('/api/chats/current/'+this.selectedChat.id, this.selectedChat, {
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization':this.tokenConBearer
                        }
                    });
                    if(response.ok){
                        this.selectChat(response);
                        this.obtenerChats();
                    }else{
                        console.log(response);
                    }
                }
                this.currentMessage = '';
            }
            
        },

        /* BUSCAR USUARIO */

        searchUser: function(event) {
            this.buscarUsuario();
        },

        async buscarUsuario() {
            this.searchError = null;
            this.possibleRecipient = null;
            if(this.newMessageRecipient == ''){
                this.searchError = "Por favor introduzca algo en el campo.";
            }else{
                let response = await fetch('/api/users/searchUser/'+this.newMessageRecipient, {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': this.tokenConBearer
                    }});
                if(response.ok) {
                    possibleUser = await response.json();
                    if(possibleUser.id != this.loggedUser.id){
                        this.possibleRecipient = possibleUser;
                    }else{
                        this.searchError = "Lo sentimos, no puedes enviarte un mensaje a ti mismo. ";
                    }
                }else{
                    this.searchError = "No se ha encontrado ningun usuario.";
                    this.possibleRecipient = null;
                }
            }
            
        },

        /* DATE FUNCTIONS */

        getDateChat(dateObj){
            date = new Date(dateObj);
            const day = date.getDate();
            const month = date.toLocaleString('default', { month: 'long' });

            return day + " " + month; 
        },
        
        isToday: function(someDate){
            const today = new Date();
            return someDate.getDate() == today.getDate() &&
                someDate.getMonth() == today.getMonth() &&
                someDate.getFullYear() == today.getFullYear();
        },

        isYesteday: function(someDate){
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);

            if (yesterday.toDateString() === someDate.toDateString()) {
                return true;
            }
            return false;
        },

        getDateMessage(dateObj){
            date = new Date(dateObj);
            dateString = '';
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            if(this.isToday(date)){
                dateString = 'Hoy';
            }else if(yesterday.toDateString() === date.toDateString()){
                dateString = 'Ayer';
            }else{
                dateString = this.getDateChat(date);
            }
            hours = this.hoursFunction(date);
            return hours + ' | ' + dateString;

        },

        hoursFunction: function (date) {   
            hours = date.getHours();
            minutes = date.getMinutes();
            ampm = 'AM';
            if(hours > 12){
                hours = hours-12;
                ampm = 'PM';
            }
            if(minutes < 10){
                minutes = '0'+minutes;
            }
            return hours + ':' + minutes + ' ' + ampm;
        },

        /*AUTHENTICATION*/ 

        getTokenBearer() {
            this.tokenConBearer = Vue.$cookies.get('TokenJWT');
            console.log(this.tokenConBearer);
            if (this.tokenConBearer===null) {
                window.location.href = './index.html';
            } else {
                this.getLoggedUser();
            }
        },
    },

    created: function () {
        this.getTokenBearer();
        this.obtenerChats();
    },
});