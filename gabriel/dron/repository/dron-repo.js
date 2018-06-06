class DroneRepo {
    constructor() {
        this.drones = {};
        this.STATUS = {
            ACTIVE: "ACTIVE",
            OFF: "OFF",
            ERROR: "ERROR"
        };
        this.locked = false;
        this.go = false;
    }

    lock() {
        this.locked = true;
    }
    unlock() {
        this.locked = false;
    }
    isLocked() {
        return this.locked;
    }
    put(uuid, dron) {
        if (this.isLocked()) return;
        if (this.get(uuid)) return this.get(uuid);
        dron.origin = {
            lat: dron.lat,
            lng: dron.lng
        }
        dron.status = this.STATUS.ACTIVE;
        dron.t = 0;
        //dron.uid = Object.keys(this.drones).length
        this.drones[uuid] = dron;
    }

    get(uuid) {
        return this.drones[uuid];
    }

    modify(uuid, dron) {
        if (!this.drones[uuid]) return;
        if (dron.lat) this.drones[uuid].lat = dron.lat;
        if (dron.lng) this.drones[uuid].lng = dron.lng;
        if (dron.h) this.drones[uuid].h = dron.h;
        if (this.STATUS[dron.status]) this.drones[uuid].status = this.STATUS[dron.status];
    }

    getAll() {
        return this.drones;
    }

    getAllAsList() {
        var objArr = Object.values(this.drones).map(function (e, i) {
            e.uid = i;
            return e;
        })
        return objArr;
    }

    goDrones(droneArray) {
        if (!this.isLocked()) return;
        droneArray.forEach(d => {
            this.drones[d.uuid].path = d.path;
            this.drones[d.uuid].gpsPath = d.gpsPath;
            this.drones[d.uuid].pixel = d.pixel;
            this.drones[d.uuid].origin_pixel = d.origin_pixel;
        });
        this.go = true;
    }

    getPath(uuid) {
        return this.go ? this.drones[uuid].gpsPath : [];
    }
    setPeriod(uuid, t) {
        if (this.go) {
            this.drones[uuid].t = t;
        }
    }
}

module.exports = DroneRepo;